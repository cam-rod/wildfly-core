/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.controller;

import org.jboss.as.controller.logging.ControllerLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

final class ThreadDumpUtil {

   public static void threadDump() {

      try (
         StringWriter dumpstr = new StringWriter();
         PrintWriter dumpout = new PrintWriter(dumpstr);
         StringWriter deadlockstr = new StringWriter();
         PrintWriter deadlockout = new PrintWriter(deadlockstr);
      ) {

         ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

         for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
            dumpout.println(threadInfoToString(threadInfo));
         }

         ControllerLogger.MGMT_OP_LOGGER.threadDump(dumpstr.toString());

         long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();

         if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(deadlockedThreads, true, true)) {
               deadlockout.println(threadInfoToString(threadInfo));
            }

            ControllerLogger.MGMT_OP_LOGGER.deadLock(deadlockstr.toString());
         }

      } catch (Exception e) {
         ControllerLogger.ROOT_LOGGER.threadDumpException(e);
      }
   }

   private static String threadInfoToString(ThreadInfo threadInfo) {
      StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" +
            " Id=" + threadInfo.getThreadId() + " " +
            threadInfo.getThreadState());
      if (threadInfo.getLockName() != null) {
         sb.append(" on " + threadInfo.getLockName());
      }
      if (threadInfo.getLockOwnerName() != null) {
         sb.append(" owned by \"" + threadInfo.getLockOwnerName() +
               "\" Id=" + threadInfo.getLockOwnerId());
      }
      if (threadInfo.isSuspended()) {
         sb.append(" (suspended)");
      }
      if (threadInfo.isInNative()) {
         sb.append(" (in native)");
      }
      sb.append('\n');
      int i = 0;
      for (; i < threadInfo.getStackTrace().length; i++) {
         StackTraceElement ste = threadInfo.getStackTrace()[i];
         sb.append("\tat " + ste.toString());
         sb.append('\n');
         if (i == 0 && threadInfo.getLockInfo() != null) {
            Thread.State ts = threadInfo.getThreadState();
            switch (ts) {
               case BLOCKED:
                  sb.append("\t-  blocked on " + threadInfo.getLockInfo());
                  sb.append('\n');
                  break;
               case WAITING:
                  sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                  sb.append('\n');
                  break;
               case TIMED_WAITING:
                  sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                  sb.append('\n');
                  break;
               default:
            }
         }

         for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
            if (mi.getLockedStackDepth() == i) {
               sb.append("\t-  locked " + mi);
               sb.append('\n');
            }
         }
      }

      LockInfo[] locks = threadInfo.getLockedSynchronizers();
      if (locks.length > 0) {
         sb.append("\n\tNumber of locked synchronizers = " + locks.length);
         sb.append('\n');
         for (LockInfo li : locks) {
            sb.append("\t- " + li);
            sb.append('\n');
         }
      }
      sb.append('\n');
      return sb.toString();
   }

}

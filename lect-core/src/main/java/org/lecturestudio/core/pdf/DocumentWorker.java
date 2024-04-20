/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.pdf;

import java.util.concurrent.LinkedBlockingQueue;

public class DocumentWorker {

    private final DocumentPlatformQueue eventQueue;

    protected LinkedBlockingQueue<DocumentWorkerTask> queue;

    protected boolean alive;

    protected Thread thread;


    public DocumentWorker(DocumentPlatformQueue eventQueue) {
        this.eventQueue = eventQueue;

        queue = new LinkedBlockingQueue<>();
        thread = new Thread(this::run);
    }

    public void start() {
        alive = true;

        thread.start();
    }

    public void stop() {
        alive = false;

        thread.interrupt();
    }

    public void add(DocumentWorkerTask task) {
        try {
            queue.put(task);
        }
        catch (InterruptedException x) {
            // Ignore
        }
    }

    public void run() {
        while (alive) {
            final DocumentWorkerTask task;

            try {
                task = queue.take();
            }
            catch (InterruptedException x) {
                break;
            }

            try {
                task.runInBackground();

                eventQueue.runInPlatformQueue(task);
            }
            catch (final Throwable t) {
                eventQueue.runInPlatformQueue(() -> task.exception(t));
            }
        }
    }
}

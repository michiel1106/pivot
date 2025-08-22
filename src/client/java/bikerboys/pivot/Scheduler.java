package bikerboys.pivot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Scheduler {
    private static final List<ScheduledTask> tasks = new LinkedList<>();

    public static void tick(MinecraftClient server) {
        Iterator<ScheduledTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            task.ticksRemaining--;

            if (task.ticksRemaining <= 0) {
                task.runnable.run();
                if (task.repeatInterval > 0) {
                    task.ticksRemaining = task.repeatInterval;
                } else {
                    iterator.remove();
                }
            }
        }
    }

    public static void runLater(Runnable runnable, int delayTicks) {
        tasks.add(new ScheduledTask(runnable, delayTicks, 0));
    }

    public static void runRepeating(Runnable runnable, int delayTicks, int intervalTicks) {
        tasks.add(new ScheduledTask(runnable, delayTicks, intervalTicks));
    }

    private static class ScheduledTask {
        private final Runnable runnable;
        private int ticksRemaining;
        private final int repeatInterval;

        private ScheduledTask(Runnable runnable, int delay, int repeatInterval) {
            this.runnable = runnable;
            this.ticksRemaining = delay;
            this.repeatInterval = repeatInterval;
        }
    }
}

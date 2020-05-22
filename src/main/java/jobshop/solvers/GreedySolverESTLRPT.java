package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class GreedySolverESTLRPT implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {

        int[] nextJobs = new int[instance.numJobs];
        int[] nextMachines = new int[instance.numMachines];

        // Initialise an object ResourceOrder from the instance to solve
        ResourceOrder resOrder = new ResourceOrder(instance);

        // Determine realisable tasks (initially first tasks of each job)
        ArrayList<Task> nextRealisableSlot = new ArrayList<>();

        // Initialise realisable tasks and realised jobs
        for (int i = 0; i < instance.numJobs; i++){
            nextRealisableSlot.add(new Task(i, 0));
        }

        while (!nextRealisableSlot.isEmpty()) {
            // We choose the task to process
            Task currentTask = null;
            ArrayList<Task> choiceTasks = new ArrayList<>();
            int minStart = Integer.MAX_VALUE;
            int start = Integer.MAX_VALUE;
            int durTask = Integer.MAX_VALUE;


            for (Task task : nextRealisableSlot) {
                start = Integer.max(nextJobs[task.job], nextMachines[instance.machine(task)]);
                if(start < minStart) {
                    minStart = start;
                    choiceTasks.clear();
                    choiceTasks.add(task);
                }
                else if (start == minStart) {
                    choiceTasks.add(task);
                }
            }

            int maxTime = Integer.MIN_VALUE;
            Task result = null;

            for (Task task : choiceTasks) {
                int processingTime = 0;
                for(int j = task.task; j < instance.numTasks; j++) {
                    processingTime += instance.duration(task.job, j);
                }
                if (processingTime > maxTime) {
                    maxTime = processingTime;
                    currentTask = task;
                }
            }

            // Remove the task in order to process it
            nextRealisableSlot.remove(currentTask);
            // Update machine and jobs dates
            int date = Integer.max(nextMachines[instance.machine(currentTask)], nextJobs[currentTask.job]);
            int updateTime = instance.duration(currentTask) + date;
            nextJobs[currentTask.job] = updateTime;
            nextMachines[instance.machine(currentTask)] = updateTime;

            if (currentTask.task < instance.numTasks - 1) {
                nextRealisableSlot.add(new Task(currentTask.job, currentTask.task+1));
            }

            int mach = instance.machine(currentTask.job, currentTask.task);
            resOrder.tasksByMachine[mach][resOrder.nextFreeSlot[mach] ++] = new Task(currentTask.job, currentTask.task);;
        }
        return new Result(instance, resOrder.toSchedule(), Result.ExitCause.Timeout);
    }
}

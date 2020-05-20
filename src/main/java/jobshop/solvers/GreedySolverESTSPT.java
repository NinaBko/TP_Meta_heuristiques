package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class GreedySolverESTSPT implements Solver {

    public Task SPT(ArrayList<Task> taskList, Instance i) {
        int durTask = Integer.MAX_VALUE;
        Task result = null;
        for (Task task : taskList) {
            if (i.duration(task.job, task.task) < durTask) {
                durTask = i.duration(task.job, task.task);
                result = task;
            }
        }
        return result;
    }

    public Task EST_SPT (ArrayList<Task> taskList, Instance i, int[] nextJobs, int[] nextMachines) {
        int minStart = Integer.MAX_VALUE;
        ArrayList<Task> choiceTasks = new ArrayList<>();
        int start = -1;
        int machine;

        for (Task task : taskList) {
            if (i.duration(task.job, task.task) < minStart) {
                machine = i.machine(task);
                start = Integer.max(nextJobs[task.job], nextMachines[machine]);
            }
            if(start < minStart) {
                minStart = start;
                choiceTasks.clear();
                choiceTasks.add(task);
            } else if (start == minStart) {
                choiceTasks.add(task);
            }
        }
        return SPT(choiceTasks, i);
    }

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

        while (!nextRealisableSlot.isEmpty() && deadline > System.currentTimeMillis()) {
            Task currentTask = null;
            currentTask = EST_SPT(nextRealisableSlot, instance, nextJobs, nextMachines);

            nextRealisableSlot.remove(currentTask);

            if (currentTask.task < instance.numTasks - 1) {
                nextRealisableSlot.add(new Task(currentTask.job, currentTask.task+1));
            }

            int mach = instance.machine(currentTask.job, currentTask.task);
            Task chosenTask = new Task(currentTask.job, currentTask.task);
            int nextFreeSlot = resOrder.nextFreeSlot[mach] ++;
            resOrder.tasksByMachine[mach][nextFreeSlot] = chosenTask;
        }
        return new Result(instance, resOrder.toSchedule(), Result.ExitCause.Blocked);
    }
}

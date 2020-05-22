package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.ArrayList;

public class GreedySolverSPT implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        // Initialise an object ResourceOrder from the instance to solve
        ResourceOrder resOrder = new ResourceOrder(instance);

        // Determine realisable tasks (initially first tasks of each job)
        ArrayList<Task> nextRealisableSlot = new ArrayList<>();

        // Initialise realisable tasks and realised jobs
        for (int i = 0; i < instance.numJobs; i++){
            nextRealisableSlot.add(new Task(i, 0));
        }

        while (!nextRealisableSlot.isEmpty()) {
            Task currentTask = null;
            int durTask = Integer.MAX_VALUE;
            Task result = null;
            for (Task task : nextRealisableSlot) {
                if (instance.duration(task.job, task.task) < durTask) {
                    durTask = instance.duration(task.job, task.task);
                    result = task;
                }
            }
            currentTask = result;

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

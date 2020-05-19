package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.Solver;
import java.util.ArrayList;

public class GreedySolverLRPT implements Solver{
    public Task SPT(ArrayList<Task> taskList, Instance i) {
        int maxTime = Integer.MIN_VALUE;
        Task result = null;

        for (Task task : taskList) {
            int processingTime = 0;
            for(int j = task.task; j < i.numTasks; j++) {
                processingTime += i.duration(task.job, j);
            }
            if (processingTime > maxTime) {
                maxTime = processingTime;
                result = task;
            }
        }
        return result;
    }

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

        while (!nextRealisableSlot.isEmpty() && deadline > System.currentTimeMillis()) {
            Task currentTask = null;
            currentTask = SPT(nextRealisableSlot, instance);

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

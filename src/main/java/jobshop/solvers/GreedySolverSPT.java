package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.ArrayList;
import java.util.List;


public class GreedySolverSPT implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        // Initialise an object ResourceOrder from the instance to solve
        ResourceOrder resOrder = new ResourceOrder(instance);

        // Determine realisable tasks (initially first tasks of each job)
        List<Integer> nextRealisableSlot = new ArrayList<>();

        // Initialise realisable tasks and realised jobs
        for (int i = 0; i < instance.numJobs; i++){
            nextRealisableSlot.add(i, 0);
        }

        int durTask = Integer.MAX_VALUE;
        Task currentTask = null;

        while (!nextRealisableSlot.isEmpty()) {
            for (int j = 0; j < nextRealisableSlot.size(); j++) {
                if (instance.duration(j, nextRealisableSlot.get(j)) < durTask) {
                    durTask = instance.duration(j, nextRealisableSlot.get(j));
                    currentTask = nextRealisableSlot.get(j);
                    currentJob = j;
                }
            }
            if (nextRealisableSlot.get(currentJob) < instance.numTasks - 1) {
                nextRealisableSlot.set(currentJob, nextRealisableSlot.get(currentJob)+1);
            }
            else {
                nextRealisableSlot.remove(currentJob);
            }

            int mach = instance.machine(currentJob, currentTask);
            Task chosenTask = new Task(currentJob, currentTask);
            resOrder.tasksByMachine[mach][currentJob] = chosenTask;
            resOrder.nextFreeSlot[mach] ++;
        }

        return new Result(instance, resOrder.toSchedule(), Result.ExitCause.Blocked);
    }
}

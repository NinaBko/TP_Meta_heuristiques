package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            // we memorize a task to swap
            Task taskMem = order.tasksByMachine[this.machine][this.t1];
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = taskMem;
        }
}

    @Override
    public Result solve(Instance instance, long deadline) {

        // generate a realizable solution
        GreedySolverESTLRPT solver = new GreedySolverESTLRPT();
        Result result = solver.solve(instance, deadline);
        Schedule initialSchedule = result.schedule;

        // memorize the best solution
        ResourceOrder bestResourceOrder = new ResourceOrder(initialSchedule);

        // initialize the makespan
        int currentMakespan = initialSchedule.makespan();

        for (Block block: blocksOfCriticalPath(bestResourceOrder)) {
            for (Swap swap: neighbors(block)) {
                ResourceOrder newResOrder = bestResourceOrder.copy();
                    swap.applyOn(newResOrder);
                    int newMakespan = newResOrder.toSchedule().makespan();
                    if (newMakespan < currentMakespan) {
                        bestResourceOrder = newResOrder;
                        currentMakespan = newMakespan;
                    } else {
                        break;
                    }
            }
        }
        return new Result(instance,bestResourceOrder.toSchedule(), Result.ExitCause.Timeout);
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        ArrayList<Block> listBlock = new ArrayList<Block>();
        List<Task> criticalPath = order.toSchedule().criticalPath();

        int currentMachine = -1;
        int previousMachine = -1;
        int indexFirstTask = 0;
        int nbTasksBlock = 0;
        int machine = 0;
        int size = criticalPath.size();
        int iter = 0;

        // iterate over the critical path
        for (Task currentTask : criticalPath) {

            // we search for the right machine for the task
            Task [][] machineSearchList = order.tasksByMachine;
            for (Task[] tasksMachine : machineSearchList) {
                for (Task taskMachine: tasksMachine) {
                    if (taskMachine.equals(currentTask)) {
                        currentMachine = machine;
                        break;
                    }
                }
                machine += 1;
            }

            // if the Task still uses the same machine
            if (currentMachine == previousMachine) {
                nbTasksBlock ++;
                if (iter == size - 1) {
                    listBlock.add(new Block(previousMachine, indexFirstTask, indexFirstTask + nbTasksBlock - 1));
                }
            }
            // if the Task runs on another machine
            else {
                // if there is more than 2 tasks in this block
                if (nbTasksBlock > 1) {
                    listBlock.add(new Block(previousMachine, indexFirstTask, indexFirstTask + nbTasksBlock - 1));
                }
                // if it's the first task of the block
                nbTasksBlock = 1;

                // we search for the right index for the task
                int currentIndex = -1;
                int index = 0;
                Task [][] taskSearchList = order.tasksByMachine;
                for (Task[] tasks : taskSearchList) {
                    for (Task task: tasks) {
                        if (task.equals(currentTask)) {
                            currentIndex = index;
                            break;
                        }
                        index += 1;
                    }
                    index = 0;
                }
                indexFirstTask = currentIndex;

                previousMachine = currentMachine;
            }
            iter += 1;
        }
        return listBlock;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        List<Swap> resultList = new ArrayList<Swap>();

        if (block.lastTask - block.firstTask == 1) {
            resultList.add(new Swap(block.machine, block.firstTask, block.lastTask));
        }
        else {
            resultList.add(new Swap(block.machine, block.firstTask, block.firstTask+1));
            resultList.add(new Swap(block.machine, block.lastTask-1, block.lastTask));
        }

        return resultList;
    }

}

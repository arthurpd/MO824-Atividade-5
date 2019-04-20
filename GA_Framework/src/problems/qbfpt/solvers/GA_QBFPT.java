package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayList;

import metaheuristics.ga.AbstractGA;
import problems.qbfpt.QBFPT;
import solutions.Solution;

/**
 * Metaheuristic GA (Genetic Algorithm) for
 * obtaining an optimal solution to a QBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). 
 * 
 * @author ccavellucci, fusberti
 */
public class GA_QBFPT extends AbstractGA<Integer, Integer> implements Runnable{

	private static int[][] results_sum = new int[7][6];
	private static int[][] results_max = new int[7][6];
	
	private int row;
	private int col;

	/**
	 * Constructor for the GA_QBF class. The QBF objective function is passed as
	 * argument for the superclass constructor.
	 * 
	 * @param generations
	 *            Maximum number of generations.
	 * @param popSize
	 *            Size of the population.
	 * @param mutationRate
	 *            The mutation rate.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public GA_QBFPT(int row, int col, Integer generations, Integer popSize, Double mutationRate, String instanceName, boolean adaptativeMutation, boolean crosspointChoice, boolean sus) throws IOException {
		super(new QBFPT("GA_Framework/instances/" + instanceName), generations, popSize, mutationRate, adaptativeMutation, crosspointChoice, sus);
		this.row = row;
		this.col = col;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#decode(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Solution<Integer> decode(Chromosome chromosome) {

		ObjFunction.makeViable(chromosome);

		Solution<Integer> solution = createEmptySol();
		for (int locus = 0; locus < chromosome.size(); locus++) {
			if (chromosome.get(locus) == 1) {
				solution.add(new Integer(locus));
			}
		}

		ObjFunction.evaluate(solution);
		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#generateRandomChromosome()
	 */
	@Override
	protected Chromosome generateRandomChromosome() {

		Chromosome chromosome = new Chromosome();
		for (int i = 0; i < chromosomeSize; i++) {
			chromosome.add(rng.nextInt(2));
		}

		return chromosome;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#fitness(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Double fitness(Chromosome chromosome) {

		if (chromosome.fitness == null)
		{
			chromosome.fitness = decode(chromosome).cost;
		}
		
		return chromosome.fitness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * metaheuristics.ga.AbstractGA#mutateGene(metaheuristics.ga.AbstractGA.
	 * Chromosome, java.lang.Integer)
	 */
	@Override
	protected void mutateGene(Chromosome chromosome, Integer locus) {

		chromosome.set(locus, 1 - chromosome.get(locus));
		chromosome.fitness = null;
	}

	/**
	 * A main method used for testing the GA metaheuristic.
	 * @throws InterruptedException 
	 * 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		String[] instances = {"qbf020","qbf040","qbf060","qbf080","qbf100","qbf200","qbf400"};
		int[] sz = {20, 40, 60, 80, 100, 200, 400};
		
		for (int i = 0; i < 7; i++)
		{
			GA_QBFPT ga_padrao = new GA_QBFPT(i, 0, 1000000000, 100, 1.0 / sz[i], instances[i], false, true, false);
			GA_QBFPT ga_pop = new GA_QBFPT(i, 1, 1000000000, 400, 1.0 / sz[i], instances[i], false, true, false);
			GA_QBFPT ga_mut = new GA_QBFPT(i, 2, 1000000000, 100, 2.0 / sz[i], instances[i], false, true, false);
			GA_QBFPT ga_crosspoint = new GA_QBFPT(i, 3, 1000000000, 100, 1.0 / sz[i], instances[i], false, false, false);
			GA_QBFPT ga_evol1 = new GA_QBFPT(i, 4, 1000000000, 100, 1.0 / sz[i], instances[i], true, true, false);
			GA_QBFPT ga_evol2 = new GA_QBFPT(i, 5, 1000000000, 100, 1.0 / sz[i], instances[i], false, true, true);
			
			ArrayList<Thread> th = new ArrayList<Thread>();
			th.add(new Thread(ga_padrao));
			th.add(new Thread(ga_pop));
			th.add(new Thread(ga_mut));
			th.add(new Thread(ga_crosspoint));
			th.add(new Thread(ga_evol1));
			th.add(new Thread(ga_evol2));
			
			for (Thread t : th)
				t.start();
		
			for (Thread t : th)
				t.join();

			System.out.print(instances[i]);
			for (int j = 0; j < 6; j++)
				System.out.printf(",%.1f", results_sum[i][j] / 10.0);
			
			System.out.println();

			System.out.print(instances[i]);
			for (int j = 0; j < 6; j++)
				System.out.print("," + results_max[i][j]);
			
			System.out.println();

		}
		
	}

	@Override
	public void run() {
		for (int i = 0; i < 10; i++)
		{
			Solution<Integer> bestSol = this.solve();
			results_sum[this.row][this.col] += bestSol.cost;
			results_max[this.row][this.col] = Math.max(results_max[this.row][this.col], bestSol.cost.intValue());
		}
		
	}

}

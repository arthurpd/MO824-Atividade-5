package problems.qbfpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import metaheuristics.ga.AbstractGA;
import problems.Evaluator;
import solutions.Solution;

public class QBFPT implements Evaluator<Integer> {

	public static final Random rng = new Random(0);

	/**
	 * Dimension of the domain.
	 */
	public final Integer size;

	/**
	 * The array of numbers representing the domain.
	 */
	public final Double[] variables;

	/**
	 * The matrix A of coefficients for the QBFPT f(x) = x'.A.x
	 */
	public Double[][] A;
	
	/**
	 * The list T of prohibited tuples
	 */
	public Integer[][] prohibited_triples;

	/**
	 * The constructor for QuadracticBinaryFunction class. The filename of the
	 * input for setting matrix of coefficients A of the QBF. The dimension of
	 * the array of variables x is returned from the {@link #readInput} method.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the QBF.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public QBFPT(String filename) throws IOException {
		size = readInput(filename);
		variables = allocateVariables();
		prohibited_triples = mountProhibitedList();
	}


	public Integer[][] mountProhibitedList() {
		Integer[][] triples = new Integer[size][3];
		for (int i = 0; i < size; i++) {
			triples[i][0] = i+1;

			if (lFunction(i, 131, 1031) != i+1) {
				triples[i][1] = lFunction(i, 131, 1031);
			} else {
				triples[i][1] = 1 + (lFunction(i, 131, 1031) % size);
			}

			Integer x = 1 + (lFunction(i, 193, 1093) % size);
			if (lFunction(i, 193, 1093) != i+1 && lFunction(i, 193, 1093) != triples[i][1]) {
				triples[i][2] = lFunction(i, 193, 1093);
			} else if (x != i+1 && x != triples[i][1]) {
				triples[i][2] = x;
			} else {
				triples[i][2] = 1 + ((lFunction(i, 193, 1093) + 1) % size);
			}
			Integer maxi = Math.max(triples[i][0], Math.max(triples[i][1], triples[i][2]));
			Integer mini = Math.min(triples[i][0], Math.min(triples[i][1], triples[i][2]));
			Integer middle = triples[i][0] + triples[i][1] + triples[i][2] - maxi - mini;
			triples[i][0] = mini - 1;
			triples[i][1] = middle - 1;
			triples[i][2] = maxi - 1;
		}
		return triples;
	}
	
	public void printProhibitedList() {
		for (int i = 0; i < size; i++) {
			System.out.println(prohibited_triples[i][0] + " " + prohibited_triples[i][1] + " " +  prohibited_triples[i][2]);
		}
	}

	private Integer lFunction(Integer u, Integer pi_1, Integer pi_2) {
		return 1 + ((pi_1 * u + pi_2) % size);
	}
		
	/**
	 * Evaluates the value of a solution by transforming it into a vector. This
	 * is required to perform the matrix multiplication which defines a QBF.
	 * 
	 * @param sol
	 *            the solution which will be evaluated.
	 */
	public void setVariables(Solution<Integer> sol) {

		resetVariables();
		if (!sol.isEmpty()) {
			for (Integer elem : sol) {
				variables[elem] = 1.0;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#getDomainSize()
	 */
	@Override
	public Integer getDomainSize() {
		return size;
	}

	/**
	 * {@inheritDoc} In the case of a QBF, the evaluation correspond to
	 * computing a matrix multiplication x'.A.x. A better way to evaluate this
	 * function when at most two variables are modified is given by methods
	 * {@link #evaluateInsertionQBF(int)}, {@link #evaluateRemovalQBF(int)} and
	 * {@link #evaluateExchangeQBF(int,int)}.
	 * 
	 * @return The evaluation of the QBF.
	 */
	@Override
	public Double evaluate(Solution<Integer> sol) {

		setVariables(sol);
		
		return sol.cost = evaluateQBF();

	}

	/**
	 * Evaluates a QBF by calculating the matrix multiplication that defines the
	 * QBF: f(x) = x'.A.x .
	 * 
	 * @return The value of the QBF.
	 */
	public Double evaluateQBF() {

		Double aux = (double) 0, sum = (double) 0;
		Double vecAux[] = new Double[size];

		for (int i = 0; i < size; i++) {
			if (variables[i] > 0.5)
			{
				for (int j = 0; j < size; j++) {
					aux += variables[j] * A[i][j];
				}

				vecAux[i] = aux;
				sum += aux * variables[i];
				aux = (double) 0;				
			}
		}

		for (int i = 0; i < prohibited_triples.length; i++) {
			if (variables[prohibited_triples[i][0]] > 0.5 && variables[prohibited_triples[i][1]] > 0.5 && variables[prohibited_triples[i][2]] > 0.5)
				sum -= 1e5;
		}

		return sum;
	}

	/**
	 * Responsible for setting the QBF function parameters by reading the
	 * necessary input from an external file. this method reads the domain's
	 * dimension and matrix {@link #A}.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the black
	 *            box function.
	 * @return The dimension of the domain.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	protected Integer readInput(String filename) throws IOException {

		Reader fileInst = new BufferedReader(new FileReader(filename));
		StreamTokenizer stok = new StreamTokenizer(fileInst);

		stok.nextToken();
		Integer _size = (int) stok.nval;
		A = new Double[_size][_size];

		for (int i = 0; i < _size; i++) {
			for (int j = i; j < _size; j++) {
				stok.nextToken();
				A[i][j] = stok.nval;
				//A[j][i] = A[i][j];
				if (j>i)
					A[j][i] = 0.0;
			}
		}

		return _size;

	}

	/**
	 * Reserving the required memory for storing the values of the domain
	 * variables.
	 * 
	 * @return a pointer to the array of domain variables.
	 */
	protected Double[] allocateVariables() {
		Double[] _variables = new Double[size];
		return _variables;
	}

	/**
	 * Reset the domain variables to their default values.
	 */
	public void resetVariables() {
		Arrays.fill(variables, 0.0);
	}

	/**
	 * Prints matrix {@link #A}.
	 */
	public void printMatrix() {

		for (int i = 0; i < size; i++) {
			for (int j = i; j < size; j++) {
				System.out.print(A[i][j] + " ");
			}
			System.out.println();
		}

	}


	@Override
	public void makeViable(AbstractGA<Integer, Integer>.Chromosome chromosome) {

		for (int i = 0; i < prohibited_triples.length; i++) {
			if (chromosome.get(prohibited_triples[i][0]) > 0.5 && chromosome.get(prohibited_triples[i][1]) > 0.5 && chromosome.get(prohibited_triples[i][2]) > 0.5)
			{
				chromosome.set(prohibited_triples[i][rng.nextInt(3)], 0);
			}
		}

	}
}

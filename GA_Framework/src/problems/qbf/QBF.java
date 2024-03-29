package problems.qbf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Arrays;

import metaheuristics.ga.AbstractGA;
import problems.Evaluator;
import solutions.Solution;

/**
 * A quadractic binary function (QBF) is a function that can be expressed as the
 * sum of quadractic terms: f(x) = \sum{i,j}{a_{ij}*x_i*x_j}. In matricial form
 * a QBF can be expressed as f(x) = x'.A.x 
 * The problem of minimizing a QBF is NP-hard [1], even when no constraints
 * are considered.
 * 
 * [1] Kochenberger, et al. The unconstrained binary quadratic programming
 * problem: a survey. J Comb Optim (2014) 28:58–81. DOI
 * 10.1007/s10878-014-9734-0.
 * 
 * @author ccavellucci, fusberti
 *
 */
public class QBF implements Evaluator<Integer> {

	/**
	 * Dimension of the domain.
	 */
	public final Integer size;

	/**
	 * The array of numbers representing the domain.
	 */
	public final Double[] variables;

	/**
	 * The matrix A of coefficients for the QBF f(x) = x'.A.x
	 */
	public Double[][] A;

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
	public QBF(String filename) throws IOException {
		size = readInput(filename);
		variables = allocateVariables();
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
			for (int j = 0; j < size; j++) {
				aux += variables[j] * A[i][j];
			}
			vecAux[i] = aux;
			sum += aux * variables[i];
			aux = (double) 0;
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

	/**
	 * A main method for testing the QBF class.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		QBF qbf = new QBF("instances/qbf040");
		qbf.printMatrix();
		Double maxVal = Double.NEGATIVE_INFINITY;
		
//		System.out.println("maxVal = " + qbf.evaluateQBF());
//		System.out.println("size = " + qbf.variables.length);
//		System.exit(0);
		

		// evaluates randomly generated values for the domain, saving the best
		// one.
		for (int i = 0; i < 10000; i++) {
			for (int j = 0; j < qbf.size; j++) {
				if (Math.random() < 0.5)
					qbf.variables[j] = 0.0;
				else
					qbf.variables[j] = 1.0;
			}
			System.out.println("x = " + Arrays.toString(qbf.variables));
			Double eval = qbf.evaluateQBF();
			System.out.println("f(x) = " + eval);
			if (maxVal < eval)
				maxVal = eval;
		}
		System.out.println("maxVal = " + maxVal);

		// evaluates the zero array.
		for (int j = 0; j < qbf.size; j++) {
			qbf.variables[j] = 0.0;
		}
		System.out.println("x = " + Arrays.toString(qbf.variables));
		System.out.println("f(x) = " + qbf.evaluateQBF());

		// evaluates the all-ones array.
		for (int j = 0; j < qbf.size; j++) {
			qbf.variables[j] = 1.0;
		}
		System.out.println("x = " + Arrays.toString(qbf.variables));
		System.out.println("f(x) = " + qbf.evaluateQBF());
		
		

	}

	@Override
	public void makeViable(AbstractGA<Integer, Integer>.Chromosome chromosome) {
		// TODO Auto-generated method stub
		
	}

}

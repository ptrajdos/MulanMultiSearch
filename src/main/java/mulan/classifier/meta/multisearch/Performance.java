/**
 * 
 */
package mulan.classifier.meta.multisearch;

import java.io.Serializable;
import java.util.ArrayList;

import mulan.evaluation.Evaluation;
import mulan.evaluation.MultipleEvaluation;
import weka.core.setupgenerator.Point;

/**
 * @author pawel trajdos
 * @since 0.1.1
 * @version 0.1.1
 *
 */
public class Performance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2799694897585301656L;
	
	/**
	 * Parameters of classifier
	 */
	protected Point<Object> values;
	
	/**
	 * Results of classifier evaluation.
	 * Stores the results of whole CV evaluation
	 */
	protected transient MultipleEvaluation mEvaluation;
	
	/**
	 * Name of the stored measure.
	 * It is assumed that we calculate only one measure.
	 */
	protected String measureName;
	
	protected boolean decreasingIsBetter=true;
	
	protected double value; 
	
	public double getPerformance(){
		return this.value;
	}

	/**
	 * @return the values
	 */
	public Point<Object> getValues() {
		return this.values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(Point<Object> values) {
		this.values = values;
	}

	/**
	 * @return the mEvaluation
	 */
	public MultipleEvaluation getmEvaluation() {
		return this.mEvaluation;
	}

	/**
	 * @param mEvaluation the mEvaluation to set
	 */
	public void setmEvaluation(MultipleEvaluation mEvaluation) {
		this.mEvaluation = mEvaluation;
		
		ArrayList<Evaluation> evList = this.mEvaluation.getEvaluations();
		Evaluation tmpEval = evList.get(0);
		this.measureName = tmpEval.getMeasures().get(0).getName();
		
		this.value = this.mEvaluation.getMean(this.measureName);
	}

	/**
	 * @return the decreasingIsBetter
	 */
	public boolean isDecreasingIsBetter() {
		return this.decreasingIsBetter;
	}

	/**
	 * @param decreasingIsBetter the decreasingIsBetter to set
	 */
	public void setDecreasingIsBetter(boolean decreasingIsBetter) {
		this.decreasingIsBetter = decreasingIsBetter;
	}

	/**
	 * @return the measureName
	 */
	public String getMeasureName() {
		return this.measureName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Performance [values=" + values + ", mEvaluation=" + mEvaluation + ", measureName=" + measureName + "]";
	}

	
	
	
	

}

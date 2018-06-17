/**
 * 
 */
package mulan.classifier.meta.multisearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import mulan.classifier.InvalidDataException;
import mulan.classifier.MultiLabelLearner;
import mulan.classifier.MultiLabelLearnerBase;
import mulan.classifier.MultiLabelOutput;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import mulan.evaluation.measure.BipartitionMeasureBase;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.Measure;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SetupGenerator;
import weka.core.TechnicalInformation;
import weka.core.setupgenerator.AbstractParameter;
import weka.core.setupgenerator.Point;
import weka.core.setupgenerator.Space;
import weka.filters.unsupervised.instance.Resample;
import weka.tools.SerialCopier;

/**
 * The class performs Grid Search for the best set of attributes for given {@link MultiLabelLearner}
 * @author Pawel Trajdos
 * @version 25.04.2016
 *
 */
public class MultiLabelMultiSearch extends MultiLabelLearnerBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -704197170000932052L;
	
	/**
	 * Learner prototype
	 */
	protected MultiLabelLearner baseLearner;
	/**
	 * Lerner which is learned with the best set of parameters
	 */
	protected MultiLabelLearner bestLearner;
	
	/**
	 * Evaluation measure for multilabel classification
	 */
	protected BipartitionMeasureBase evalMeasure;
	/**
	 * if isDecreasingBetter is true, then we assume that lower values of measure indicate better result.
	 */
	protected boolean isDecreasingBetter=true;
	
	
	
	/**
	 * The best values
	 */
	protected Point<Object> values;
	
	/**
	 * Attribute search space
	 */
	protected Space space;
	
	/**
	 * Search Setup Generator
	 */
	protected SetupGenerator setupGen;
	
	protected Vector<Performance> performances;
	  
	  /**
	   *The number of folds in the initial search
	   */
	  protected int initialSpaceNumFolds=2;
	  /**
	   * The number of folds in the subsequent search
	   */
	  protected int subsequentSpaceNumFolds=5;
	  
	  /**
	   * if singlerun is true, only initial experiment is performed
	   */
	  protected boolean singleRun=false;
	  
	  protected boolean uniformPerformance=false;
	  

	  /**
	   * Default constructor
	   */
	  public MultiLabelMultiSearch(){
		  this.setupGen = new SetupGenerator();
		  this.setupGen.setBaseObject(this);
		  this.evalMeasure = new HammingLoss();
	  }

	@Override
	protected void buildInternal(MultiLabelInstances trainingSet) throws Exception {
		Point<Object>	evals;
		MultiLabelMultiSearch multi;

		this.performances = new Vector<Performance>();

	   	this.setupGen.reset();
	    this.space = this.setupGen.getSpace();
	    
	    MultiLabelInstances setCopy = new MultiLabelInstances(new Instances(trainingSet.getDataSet()), trainingSet.getLabelsMetaData());
	    this.values = findBest(setCopy);
	
	    // setup best configurations
	    ///evals            = this.setupGen.evaluate(this.values);
	    multi            = (MultiLabelMultiSearch) this.setupGen.setup(this, values);
	    this.bestLearner = multi.getBaseLearner();
	
	    // train classifier
	    
	    this.baseLearner = (MultiLabelLearner) SerialCopier.makeCopy(this.bestLearner);
	    this.bestLearner.build(trainingSet);

	 
	}

	@Override
	protected MultiLabelOutput makePredictionInternal(Instance instance) throws Exception, InvalidDataException {
		return this.bestLearner.makePrediction(instance);
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Determines best solution under the search space
	 * @param space -- parameter search space
	 * @param data -- validation data
	 * @param folds -- the number of folds
	 * @return -- the best parameter combination
	 * @throws Exception
	 */
	protected Point<Object> determineBestInSpace(Space space, MultiLabelInstances data, int folds) throws Exception {
		 Point<Object>		result = null;
		    Enumeration<Point<Object>>	enm;
		    Performance			p1;
		    Performance			p2;
		    Point<Object>		values;
		    this.performances.clear();
		    enm = this.space.values();
		   
		    Evaluator evaluator = new Evaluator();
		    List<Measure> measureList = new ArrayList<Measure>();
		    measureList.add(evalMeasure);
		    
		    MultipleEvaluation mEval;
		    Performance perf = null;
		    Point<Object> mevals;
		    MultiLabelMultiSearch multi;
		    MultiLabelLearner tmpLearner;
		    while (enm.hasMoreElements()) {
		      values = enm.nextElement();
		     
		      perf = new Performance();
		      mevals = this.setupGen.evaluate(values);
		      multi = (MultiLabelMultiSearch) this.setupGen.setup(this, mevals);
		      tmpLearner = multi.getBaseLearner();
		      
		      mEval = evaluator.crossValidate(tmpLearner, data, measureList, folds);
		      perf.setmEvaluation(mEval);
		      perf.setValues(mevals);
		      perf.setDecreasingIsBetter(this.isDecreasingBetter);
		      this.performances.add(perf);
		      
		      debug("Vals: "+mevals+"\t Crit:" + perf.getPerformance() );
		    }
		    // sort list
		    Collections.sort(this.performances, new PerformanceComparator());

		    result = this.performances.firstElement().getValues();
		    
		    this.debug("Best Val: "+result + "\t Crit: "+this.performances.firstElement().getPerformance());

		    // check whether all performances are the same
		    this.uniformPerformance = true;
		    p1 = this.performances.get(0);
		    for (int i = 1; i < this.performances.size(); i++) {
		      p2 = this.performances.get(i);
		      if (p2.getPerformance() != p1.getPerformance()) {
		    	  this.uniformPerformance = false;
		    	  break;
		      }
		    }
		    this.performances.clear();

		    return result;
	}
	/**
	 * Find best solution using dataset
	 * @param inst -- dataset
	 * @return
	 * @throws Exception
	 */
	protected Point<Object> findBest(MultiLabelInstances inst) throws Exception {
		Point<Integer>	center;
	    Space		neighborSpace;
	    boolean		finished;
	    Point<Object>	evals;
	    Point<Object>	result;
	    Point<Object>	resultOld;
	    int			iteration;
	    Instances		sample;
	    Resample		resample;
	    MultiLabelMultiSearch		multi;

	    iteration            = 0;
	    this.uniformPerformance = false;

	    // find first center
	    
	    result = determineBestInSpace(this.space, inst, this.initialSpaceNumFolds);
	    

	    finished = this.uniformPerformance | this.singleRun;

	    if (!finished) {
	      do {
		iteration++;
		resultOld = (Point<Object>) result.clone();
		center    = this.space.getLocations(result);
		// on border? -> finished
		if (this.space.isOnBorder(center)) {
		  finished = true;
		}

		// new space with current best one at center and immediate neighbors
		// around it
		if (!finished) {
		  neighborSpace = this.space.subspace(center);
		  result = determineBestInSpace(neighborSpace, inst, this.subsequentSpaceNumFolds);
		  
		  finished = this.uniformPerformance;

		  // no improvement?
		  if (result.equals(resultOld)) {
		    finished = true;
		    
		  }
		}
	      }
	      while (!finished);
	    }
	    
	    evals = this.setupGen.evaluate(result);
	    multi = (MultiLabelMultiSearch) this.setupGen.setup(this, evals);
	    
	    return result;
	}
	
	/**
	 * Set parameters
	 * @param value
	 */
	public void setSearchParameters(AbstractParameter[] value){
		this.setupGen.setParameters(value.clone());
	}
	
	/**
	 * Get parameters
	 * @return
	 */
	public AbstractParameter[] getSearchParameters(){
		return this.setupGen.getParameters();
	}
	/**
	 * Set base multilabel learner
	 * @param learner
	 */
	public void setLearner(MultiLabelLearner learner){
		this.baseLearner = learner;
	}

	/**
	 * Get evaluation measure
	 * @return the evalMeasure
	 */
	public BipartitionMeasureBase getEvalMeasure() {
		return this.evalMeasure;
	}

	/**
	 * Set evaluation measure
	 * @param evalMeasure the evalMeasure to set
	 */
	public void setEvalMeasure(BipartitionMeasureBase evalMeasure) {
		this.evalMeasure = evalMeasure;
	}

	/**
	 * Get the number of folds for the initial search
	 * @return the initialSpaceNumFolds
	 */
	public int getInitialSpaceNumFolds() {
		return this.initialSpaceNumFolds;
	}

	/**
	 * Set the number of folds for the initial search
	 * @param initialSpaceNumFolds the initialSpaceNumFolds to set
	 */
	public void setInitialSpaceNumFolds(int initialSpaceNumFolds) {
		this.initialSpaceNumFolds = initialSpaceNumFolds;
	}

	/**
	 * Get the number of folds fot the subsequent evaluation
	 * @return the subsequentSpaceNumFolds
	 */
	public int getSubsequentSpaceNumFolds() {
		return this.subsequentSpaceNumFolds;
	}

	/**
	 * @param subsequentSpaceNumFolds the subsequentSpaceNumFolds to set
	 */
	public void setSubsequentSpaceNumFolds(int subsequentSpaceNumFolds) {
		this.subsequentSpaceNumFolds = subsequentSpaceNumFolds;
	}

	/**
	 * Get the learner trained using the best determined parameter combination
	 * @return the bestLearner
	 */
	public MultiLabelLearner getBestLearner() {
		return this.bestLearner;
	}

	/**
	 * Get the search space
	 * @return the values
	 */
	public Point<Object> getValues() {
		return this.values;
	}

	/**
	 * Get the base learner
	 * @return the baseLearner
	 */
	public MultiLabelLearner getBaseLearner() {
		return this.baseLearner;
	}

	/**
	 * Set the base learner
	 * @param baseLearner the baseLearner to set
	 */
	public void setBaseLearner(MultiLabelLearner baseLearner) {
		this.baseLearner = baseLearner;
	}

	/**
	 * Determines whether only the first run is performed
	 * @return the singleRun
	 */
	public boolean isSingleRun() {
		return this.singleRun;
	}

	/**
	 * @param singleRun the singleRun to set
	 */
	public void setSingleRun(boolean singleRun) {
		this.singleRun = singleRun;
	}

	/**
	 * Determines if the measure indicates high classification quality for lowe measure value.
	 * @return the isDecreasingBetter
	 */
	public boolean isDecreasingBetter() {
		return this.isDecreasingBetter;
	}

	/**
	 * @param isDecreasingBetter the isDecreasingBetter to set
	 */
	public void setDecreasingBetter(boolean isDecreasingBetter) {
		this.isDecreasingBetter = isDecreasingBetter;
	}
	
	
	

}

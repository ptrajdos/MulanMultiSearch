/**
 * 
 */
package mulan.classifier.meta.multisearch;

import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;

import mulan.classifier.InvalidDataException;
import mulan.classifier.MultiLabelLearner;
import mulan.classifier.MultiLabelLearnerBase;
import mulan.classifier.MultiLabelOutput;
import mulan.data.MultiLabelInstances;
import weka.core.Instance;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;

/**
 * @author pawel
 *
 */
public class FieldGetterSetter extends MultiLabelLearnerBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8683111854482394487L;
	
	protected MultiLabelLearner baseLearner;
	
	protected String fieldName;

	/**
	 * 
	 */
	public FieldGetterSetter(MultiLabelLearner baseLerner, String fieldName) {
		this.baseLearner = baseLerner;
		this.fieldName = fieldName;
	}

	/* (non-Javadoc)
	 * @see mulan.classifier.MultiLabelLearnerBase#buildInternal(mulan.data.MultiLabelInstances)
	 */
	@Override
	protected void buildInternal(MultiLabelInstances trainingSet) throws Exception {
		this.baseLearner.build(trainingSet);

	}

	/* (non-Javadoc)
	 * @see mulan.classifier.MultiLabelLearnerBase#makePredictionInternal(weka.core.Instance)
	 */
	@Override
	protected MultiLabelOutput makePredictionInternal(Instance instance) throws Exception, InvalidDataException {
		
		return this.baseLearner.makePrediction(instance);
	}

	/* (non-Javadoc)
	 * @see mulan.classifier.MultiLabelLearnerBase#getTechnicalInformation()
	 */
	@Override
	public TechnicalInformation getTechnicalInformation() {
		if(this.baseLearner instanceof TechnicalInformationHandler){
			return ((TechnicalInformationHandler) this.baseLearner).getTechnicalInformation();
		}
		return null;
	}
	
	public Object getField() throws Exception{
		Field field = FieldUtils.getField(this.baseLearner.getClass(), this.fieldName,true);
		field.setAccessible(true);
		return field.get(this.baseLearner);
	}
	
	public void setField(Object obj)throws Exception{
		Field field = FieldUtils.getField(this.baseLearner.getClass(), this.fieldName,true);
		field.setAccessible(true);
		field.set(this.baseLearner, obj);
	}

}

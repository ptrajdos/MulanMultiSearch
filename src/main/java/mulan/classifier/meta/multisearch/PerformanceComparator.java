/**
 * 
 */
package mulan.classifier.meta.multisearch;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Class implements comparator for {@link Performance} objects
 * @author pawel
 *
 */
public class PerformanceComparator implements Serializable, Comparator<Performance> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4007162617389000668L;


	@Override
	public int compare(Performance o1, Performance o2) {
		 if(!o1.getMeasureName().equals(o2.getMeasureName()))
			 throw new IllegalArgumentException("Incompatible measures");
		 
		 double p1,p2;
		 int result=0;
		 p1 = o1.getPerformance();
		 p2 = o2.getPerformance();
		 
		 if(p1<p2)result=-1;
		 else if(p1>p2) result=1;
		 else result=0;
		 
		 if(!o1.decreasingIsBetter){
			 result = -result;
		 }
		 
		return result;
	}

}

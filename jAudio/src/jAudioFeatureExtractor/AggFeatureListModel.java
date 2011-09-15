/**
 *
 */
package jAudioFeatureExtractor;

import javax.swing.table.DefaultTableModel;

/**
 * @author mcennis
 *
 */
public class AggFeatureListModel extends DefaultTableModel {

	public AggFeatureListModel(){
		super();
	}

	public AggFeatureListModel(Object[] o,int rows){
		super(o,rows);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}


}

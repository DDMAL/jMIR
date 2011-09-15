/**
 *
 */
package jAudioFeatureExtractor;

import java.util.Vector;
import jAudioFeatureExtractor.Aggregators.Aggregator;

/**
 * @author mcennis
 *
 */
public class AggListTableModel extends javax.swing.table.DefaultTableModel{

	private Vector<Aggregator> agg;

	public AggListTableModel(){
		super(new Object[]{"Global","Name"},2);
		agg = new Vector<Aggregator>();

	}

	public void init(java.util.HashMap<String, Aggregator> data){
		agg.clear();
		super.dataVector.clear();
		agg.addAll(data.values());
		for(int i=0;i<agg.size();++i){
			Vector row = new Vector();
			row.add(new Boolean(agg.get(i).getAggregatorDefinition().generic));
			row.add(agg.get(i).getAggregatorDefinition().name);
			dataVector.add(row);
		}
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}


	@Override
	public void removeRow(int row) {
		agg.remove(row);
		super.removeRow(row);
	}

	public void clear(){
		super.dataVector.clear();
		agg.clear();
	}

	public Aggregator getAggregator(int row){
		return agg.elementAt(row);
	}

}

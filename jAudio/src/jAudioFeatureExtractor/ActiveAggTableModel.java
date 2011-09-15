/**
 *
 */
package jAudioFeatureExtractor;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import jAudioFeatureExtractor.Aggregators.Aggregator;

/**
 * @author mcennis
 *
 */
public class ActiveAggTableModel extends DefaultTableModel {

	Vector<Aggregator> agg;

	public ActiveAggTableModel() {
		super(new Object[] { "Name" }, 0);
		agg = new Vector<Aggregator>();
	}

	public Aggregator getAggregator(int row) {
		return agg.get(row);
	}

	public Aggregator[] getAggregator() {
		return agg.toArray(new Aggregator[] {});
	}

	public void setAggregator(int row, Aggregator a, boolean edited) {
		agg.set(row, a);
		this.setValueAt(a.getAggregatorDefinition().name, row, 0);
		fireTableCellUpdated(row, 0);
	}

	public void addAggregator(Aggregator a) {
		agg.add(a);
		this.addRow(new Object[] { a.getAggregatorDefinition().name });
	}

	public void removeAggregator(int row) {
		agg.remove(row);
		this.removeRow(row);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void init(Controller c) {
		if (c.dm_.aggregators != null) {
			for (int i = 0; i < c.dm_.aggregators.length; ++i) {
				agg.add(c.dm_.aggregators[i]);
				this.addRow(new Object[] {
						c.dm_.aggregators[i].getAggregatorDefinition().name });
			}
		}
	}
}

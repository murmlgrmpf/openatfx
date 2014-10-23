package de.rechner.openatfx;

import java.util.ArrayList;
import java.util.List;

import org.asam.ods.AoException;
import org.asam.ods.Column;
import org.asam.ods.ErrorCode;
import org.asam.ods.InstanceElement;
import org.asam.ods.InstanceElementIterator;
import org.asam.ods.NameUnit;
import org.asam.ods.NameValue;
import org.asam.ods.NameValueSeqUnit;
import org.asam.ods.NameValueUnit;
import org.asam.ods.NameValueUnitIterator;
import org.asam.ods.Relationship;
import org.asam.ods.SetType;
import org.asam.ods.SeverityFlag;
import org.asam.ods.TS_ValueSeq;
import org.asam.ods.ValueMatrixMode;
import org.asam.ods.ValueMatrixPOA;


/**
 * Implementation of <code>org.asam.ods.ValueMatrix</code>.
 * 
 * @author Christian Rechner
 */
class ValueMatrixOnSubMatrixImpl extends ValueMatrixPOA {

    private final SubMatrixImpl sourceSubMatrix;
    private final ValueMatrixMode mode;

    /**
     * Constructor.
     * 
     * @param sourceSubMatrix The SubMatrix object.
     * @param mode The ValueMatrixMode.
     */
    public ValueMatrixOnSubMatrixImpl(SubMatrixImpl sourceSubMatrix, ValueMatrixMode mode) {
        this.sourceSubMatrix = sourceSubMatrix;
        this.mode = mode;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getMode()
     */
    public ValueMatrixMode getMode() throws AoException {
        return this.mode;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getColumnCount()
     */
    public int getColumnCount() throws AoException {
        InstanceElementIterator iter = sourceSubMatrix.getRelatedInstancesByRelationship(Relationship.CHILD, "*");
        int cnt = iter.getCount();
        iter.destroy();
        return cnt;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getRowCount()
     */
    public int getRowCount() throws AoException {
        NameValueUnit nvu = this.sourceSubMatrix.getValueByBaseName("number_of_rows");
        if (nvu.value.flag == 15) {
            return nvu.value.u.longVal();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#listColumns(java.lang.String)
     */
    public String[] listColumns(String colPattern) throws AoException {
        InstanceElementIterator iter = sourceSubMatrix.getRelatedInstancesByRelationship(Relationship.CHILD, colPattern);
        InstanceElement[] ies = iter.nextN(iter.getCount());
        iter.destroy();

        List<String> list = new ArrayList<String>(ies.length);
        for (int i = 0; i < ies.length; i++) {
            list.add(ies[i].getName());
            ies[i].destroy();
        }
        return list.toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#listIndependentColumns(java.lang.String)
     */
    public String[] listIndependentColumns(String colPattern) throws AoException {
        InstanceElementIterator iter = sourceSubMatrix.getRelatedInstancesByRelationship(Relationship.CHILD, colPattern);
        InstanceElement[] ies = iter.nextN(iter.getCount());
        iter.destroy();

        List<String> list = new ArrayList<String>(ies.length);
        for (int i = 0; i < ies.length; i++) {
            NameValueUnit nvu = ies[i].getValueByBaseName("independent");
            if (nvu.value.flag == 15 && nvu.value.u.shortVal() > 0) {
                list.add(ies[i].getName());
            }
            ies[i].destroy();
        }
        return list.toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getColumns(java.lang.String)
     */
    public Column[] getColumns(String colPattern) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getIndependentColumns(java.lang.String)
     */
    public Column[] getIndependentColumns(String colPattern) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getValueMeaPoint(int)
     */
    public NameValueUnitIterator getValueMeaPoint(int meaPoint) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getValueVector(org.asam.ods.Column, int, int)
     */
    public TS_ValueSeq getValueVector(Column col, int startPoint, int count) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#removeValueMeaPoint(java.lang.String[], int, int)
     */
    public void removeValueMeaPoint(String[] columnNames, int meaPoint, int count) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#removeValueVector(org.asam.ods.Column, int, int)
     */
    public void removeValueVector(Column col, int startPoint, int count) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#setValueMeaPoint(org.asam.ods.SetType, int, org.asam.ods.NameValue[])
     */
    public void setValueMeaPoint(SetType set, int meaPoint, NameValue[] value) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#setValueVector(org.asam.ods.Column, org.asam.ods.SetType, int,
     *      org.asam.ods.TS_ValueSeq)
     */
    public void setValueVector(Column col, SetType set, int startPoint, TS_ValueSeq value) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#setValue(org.asam.ods.SetType, int, org.asam.ods.NameValueSeqUnit[])
     */
    public void setValue(SetType set, int startPoint, NameValueSeqUnit[] value) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#addColumn(org.asam.ods.NameUnit)
     */
    public Column addColumn(NameUnit newColumn) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#listScalingColumns(java.lang.String)
     */
    public String[] listScalingColumns(String colPattern) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getScalingColumns(java.lang.String)
     */
    public Column[] getScalingColumns(String colPattern) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#listColumnsScaledBy(org.asam.ods.Column)
     */
    public String[] listColumnsScaledBy(Column scalingColumn) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getColumnsScaledBy(org.asam.ods.Column)
     */
    public Column[] getColumnsScaledBy(Column scalingColumn) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#addColumnScaledBy(org.asam.ods.NameUnit, org.asam.ods.Column)
     */
    public Column addColumnScaledBy(NameUnit newColumn, Column scalingColumn) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#getValue(org.asam.ods.Column[], int, int)
     */
    public NameValueSeqUnit[] getValue(Column[] columns, int startPoint, int count) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ValueMatrixOperations#destroy()
     */
    public void destroy() throws AoException {
        // do nothing
    }

}

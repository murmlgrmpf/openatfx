package de.rechner.openatfx;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asam.ods.AoException;
import org.asam.ods.ErrorCode;
import org.asam.ods.InstanceElement;
import org.asam.ods.InstanceElementIteratorPOA;
import org.asam.ods.SeverityFlag;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;


/**
 * Implementation of <code>org.asam.ods.InstanceElementIterator</code>.
 * 
 * @author Christian Rechner
 */
class InstanceElementIteratorImpl extends InstanceElementIteratorPOA {

    private static final Log LOG = LogFactory.getLog(InstanceElementIteratorImpl.class);

    private final POA poa;
    private final InstanceElement[] instanceElements;
    private int pointer;

    /**
     * Constructor.
     * 
     * @param poa The POA.
     * @param instanceElements The instance elements.
     */
    public InstanceElementIteratorImpl(POA poa, InstanceElement[] instanceElements) {
        this.poa = poa;
        this.instanceElements = instanceElements;
        this.pointer = 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.InstanceElementIteratorOperations#getCount()
     */
    public int getCount() throws AoException {
        return this.instanceElements.length;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.InstanceElementIteratorOperations#nextN(int)
     */
    public InstanceElement[] nextN(int how_many) throws AoException {
        List<InstanceElement> list = new LinkedList<InstanceElement>();
        for (int i = pointer; i < how_many; i++) {
            if (i >= this.instanceElements.length) {
                break;
            }
            list.add(this.instanceElements[i]);
            this.pointer++;
        }
        return list.toArray(new InstanceElement[0]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.InstanceElementIteratorOperations#nextOne()
     */
    public InstanceElement nextOne() throws AoException {
        if (pointer >= this.instanceElements.length) {
            throw new AoException(ErrorCode.AO_IMPLEMENTATION_PROBLEM, SeverityFlag.ERROR, 0, "Iterator is at the end");
        }
        InstanceElement ie = this.instanceElements[this.pointer];
        pointer++;
        return ie;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.InstanceElementIteratorOperations#reset()
     */
    public void reset() throws AoException {
        this.pointer = 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.InstanceElementIteratorOperations#destroy()
     */
    public void destroy() throws AoException {
        try {
            byte[] id = poa.servant_to_id(this);
            poa.deactivate_object(id);
        } catch (WrongPolicy e) {
            LOG.error(e.getMessage(), e);
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0, e.getMessage());
        } catch (ObjectNotActive e) {
            LOG.error(e.getMessage(), e);
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0, e.getMessage());
        } catch (ServantNotActive e) {
            LOG.error(e.getMessage(), e);
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0, e.getMessage());
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tk.nomis_tech.ppimapbuilder.psicquicclient;

import org.hupo.psi.mi.psicquic.wsclient.AbstractPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import org.hupo.psi.mi.psicquic.wsclient.QueryOperand;

/**
 *
 * @author dsi-nomistech
 */
public class UPPsicquicClient extends AbstractPsicquicClient{

    public UPPsicquicClient(String serviceAddress) {
        super(serviceAddress);
    }

    @Override
    public Object getByQuery(String string, int i, int i1) throws PsicquicClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getByInteractor(String string, int i, int i1) throws PsicquicClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getByInteraction(String string, int i, int i1) throws PsicquicClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getByInteractionList(String[] strings, int i, int i1) throws PsicquicClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getByInteractorList(String[] strings, QueryOperand qo, int i, int i1) throws PsicquicClientException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}

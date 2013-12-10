package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import tk.nomis_tech.ppimapbuilder.util.PsicquicRegistry;
import tk.nomis_tech.ppimapbuilder.util.PsicquicService;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    public QueryWindow() {
        setTitle("Interaction Query");

        JButton startQuery = new JButton("Start");
        startQuery.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    // official registry
                    PsicquicRegistry registry = new PsicquicRegistry();
                    registry.retrieveServices();
                    registry.remove("genemania"); // remove problematic (and dirty) DB

                    for (PsicquicService service : registry.getServices()) {

//                        System.out.println(service.toString());
                        System.out.println("----- >>> " + service.getName() + "----------------------");
                        PsicquicSimpleClient client = new PsicquicSimpleClient(service.getRestUrl());
                        PsimiTabReader mitabReader = new PsimiTabReader();
                        InputStream result = client.getByInteractor("P04040");
                        Collection<BinaryInteraction> binaryInteractions = mitabReader.read(result);
                        System.out.println("Interactions found: " + binaryInteractions.size());
                        System.out.println("---------------------------------------");
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (PsimiTabException ex) {
                    Logger.getLogger(QueryWindow.class.getName()).log(Level.SEVERE, null, ex);
                }

                QueryWindow.this.setVisible(false);
                QueryWindow.this.dispose();
            }

        });

        getContentPane().add(startQuery);
        getRootPane().setDefaultButton(startQuery);

        setBounds(0, 0, 200, 100);
        setLocationRelativeTo(null);
    }
}

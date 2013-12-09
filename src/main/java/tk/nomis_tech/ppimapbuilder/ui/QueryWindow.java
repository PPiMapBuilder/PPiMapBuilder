package tk.nomis_tech.ppimapbuilder.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;

import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * PPiMapBuilder interaction query window
 */
public class QueryWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    public QueryWindow() {
        setTitle("Interaction Query");

        JButton startQuery = new JButton("Start");
        startQuery.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    PsicquicSimpleClient client = new PsicquicSimpleClient("http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/");

                    PsimiTabReader mitabReader = new PsimiTabReader();

                    InputStream result = client.getByQuery("brca2");

                    Collection<BinaryInteraction> binaryInteractions = mitabReader.read(result);

                    System.out.println("Interactions found: " + binaryInteractions.size());

                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (PsimiTabException e1) {
                    e1.printStackTrace();
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

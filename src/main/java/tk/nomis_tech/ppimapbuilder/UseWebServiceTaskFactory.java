package tk.nomis_tech.ppimapbuilder;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class UseWebServiceTaskFactory extends AbstractTaskFactory {

    private final WebServiceHelper webServiceHelper;

    public UseWebServiceTaskFactory(WebServiceHelper webServiceHelper) {
        this.webServiceHelper = webServiceHelper;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new UseWebServiceTask(webServiceHelper));
    }
}

package ro.jmind.photos.commands;

import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ro.jmind.photos.model.UploadDetail;
import ro.jmind.photos.service.DataService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ShellComponent
public class CamelCommands {
    public static final String DIRECT_DEFAULT_ROUTE = "direct:defaultRoute";
    public static final String DIRECT_MAIN_ROUTE = "direct:mainRoute";
    public static final String SEDA_MAIN_ROUTE = "seda:mainRoute";
    private static Logger LOGGER = LoggerFactory.getLogger(CamelCommands.class);

    private DataService dataService;
    private ProducerTemplate template;

    public CamelCommands(DataService dataService, ProducerTemplate template) {
        this.dataService = dataService;
        this.template = template;

        template.setDefaultEndpointUri(DIRECT_DEFAULT_ROUTE);
    }

    @ShellMethod("create basic json data")
    private String gatherDataFromLocal() {
        Processor processor = exchange -> {
            File baseDir = new File("D:\\OneDrive\\_backup\\photos\\seba");
            Map<String, List<UploadDetail>> albumNameUploadDetailsMap = dataService.collectLocalData(baseDir);

            exchange.getIn().setBody(albumNameUploadDetailsMap);
        };
        template.send("direct:gatherLocalData", processor);
        return "done creating basic json data";
    }

    @ShellMethod("Starts main route")
    public String startMainRoute() {

        Processor createUploadDetailsListProcessor = exchange -> {
//            String fromRouteId = "direct:routeOneId";
//            LOGGER.info("setting exchange fromRouteId to {}", fromRouteId);
//            exchange.setFromRouteId(fromRouteId);

            List<UploadDetail> uploadDetails = new ArrayList<>();
            UploadDetail uploadDetail = new UploadDetail();

            uploadDetail.setAlbumName(String.format("album_%s", (int) (Math.random() * 10000)));
            uploadDetails.add(uploadDetail);

            uploadDetail = new UploadDetail();
            uploadDetail.setAlbumName(String.format("album_%s", (int) (Math.random() * 10000)));
            uploadDetails.add(uploadDetail);

            uploadDetail = new UploadDetail();
            uploadDetail.setAlbumName(String.format("album_%s", (int) (Math.random() * 10000)));
            uploadDetails.add(uploadDetail);

            //add the list to body
            exchange.getIn().setBody(uploadDetails);
        };

        template.send(DIRECT_MAIN_ROUTE, createUploadDetailsListProcessor);
        return "shell command ended";
    }

}

package gash.router.server.resources;

import com.google.protobuf.GeneratedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by rushil on 4/2/16.
 */
public interface Resource {

    Logger logger = LoggerFactory.getLogger("Resource Handling");

    void handleCommand(Pipe.CommandRequest msg);

    void handleWork(Work.WorkRequest msg);
}

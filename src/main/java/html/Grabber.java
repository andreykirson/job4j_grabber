package html;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private ConfigManager cfg;


    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public Store store() throws IOException {
        return new PsqlStore(cfg);
    }

    public void cfg() {
        cfg = new ConfigManager("grabber.properties");
    }


    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.get("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            Timestamp lastDate = store.lastItem();
            Predicate<Timestamp> until = (date) -> date.before(lastDate);
            try {
                for (String resource : parse.resources()) {
                    List<Post> listPost = parse.list(resource, until);
                    Iterator<Post> it = listPost.iterator();
                    while (it.hasNext()) {
                        store.save(it.next());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.get("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes());
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(), store, scheduler);
        grab.web(store);
    }

}
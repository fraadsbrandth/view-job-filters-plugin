package hudson.views;

import static org.junit.Assert.assertEquals;
import hudson.model.Result;
import hudson.model.TopLevelItem;
import hudson.model.AllView;
import hudson.model.FreeStyleProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.RunLoadCounter;

public class AbstractBuildTrendFilterTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Bug(18986)
    @Test
    @Ignore(value = "Failing on Java 7 - Never worked on Fork")
    public void lazyLoading() throws Exception {
        final FreeStyleProject p1 = j.createFreeStyleProject("p1");
        RunLoadCounter.prepare(p1);
        p1.getBuildersList().add(new FailureBuilder());
        for (int i = 0; i < 5; i++) {
            j.assertBuildStatus(Result.FAILURE, p1.scheduleBuild2(0).get());
        }
        final FreeStyleProject p2 = j.createFreeStyleProject("p2");
        j.assertBuildStatusSuccess(p2.scheduleBuild2(0));
        final ViewJobFilter filter = new BuildTrendFilter("AtLeastOne", "Stable", 3, "Builds", "includeMatched");
        assertEquals(Collections.singletonList(p2), RunLoadCounter.assertMaxLoads(p1, 3, new Callable<List<TopLevelItem>>() {
            @Override public List<TopLevelItem> call() throws Exception {
                return filter.filter(new ArrayList<TopLevelItem>(), Arrays.<TopLevelItem>asList(p1, p2), new AllView("_"));
            }
        }));
    }

}
package jenkins.plugins.logstash.persistence;

import com.google.gson.*;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.lang.reflect.Type;
import java.util.List;

public class SCMGsonSerializer implements JsonSerializer<SCM> {
    @Override
    public JsonElement serialize(SCM scm, Type type, JsonSerializationContext jsonSerializationContext) {
        if (scm instanceof GitSCM) {
            return serialize((GitSCM) scm);
        }
        return new Gson().toJsonTree(scm);
    }

    private JsonElement serialize(GitSCM scm) {
        JsonObject json = new JsonObject();
        json.add("branches", getBranches(scm));
        json.add("repos", getRepositoriesURIs(scm));
        return json;
    }

    private JsonArray getRepositoriesURIs(GitSCM scm) {
        List<RemoteConfig> repos = scm.getRepositories();
        JsonArray jsonRepos = new JsonArray();
        for (RemoteConfig remote : repos) {
            for (URIish uri : remote.getURIs()) {
                jsonRepos.add(new JsonPrimitive(uri.toString()));
            }
        }
        return jsonRepos;
    }

    private JsonArray getBranches(GitSCM scm) {
        List<BranchSpec> branches = scm.getBranches();
        JsonArray jsonBranches = new JsonArray();
        for (BranchSpec branch : branches) {
            jsonBranches.add(new JsonPrimitive(branch.getName()));
        }
        return jsonBranches;
    }
}

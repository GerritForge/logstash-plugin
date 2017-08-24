/*
 * The MIT License
 *
 * Copyright 2017 GerritForge Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.logstash;

import hudson.Extension;
import hudson.model.*;
import jenkins.branch.BranchProperty;
import jenkins.branch.BranchPropertyDescriptor;
import jenkins.branch.JobDecorator;
import jenkins.branch.MultiBranchProjectDescriptor;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.export.Exported;

import java.util.Iterator;
import java.util.List;

public class LogstashWrapperBranchPropertyDefinition extends BranchProperty {
    // ParameterDefinitionBranchProperty {

    private List<ParameterDefinition> parameterDefinitions;

    @Exported
    public final List<ParameterDefinition> getParameterDefinitions() {
        return this.parameterDefinitions;
    }

    @DataBoundSetter
    public final void setParameterDefinitions(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    protected <P extends Job<P, B>, B extends Run<P, B>> boolean isApplicable(Class<P> clazz) {
        return Job.class.isAssignableFrom(clazz) && ParameterizedJobMixIn.ParameterizedJob.class.isAssignableFrom(clazz);
    }

    public final <P extends Job<P, B>, B extends Run<P, B>> JobDecorator<P, B> jobDecorator(Class<P> clazz) {
        return !this.isApplicable(clazz) ? null : new JobDecorator<P, B>() {

            public List<JobProperty<? super P>> jobProperties(List<JobProperty<? super P>> jobProperties) {
                List<JobProperty<? super P>> result = BranchProperty.asArrayList(jobProperties);
                Iterator iterator = result.iterator();

                while (iterator.hasNext()) {
                    JobProperty<? super P> p = (JobProperty) iterator.next();
                    if (p instanceof ParametersDefinitionProperty) {
                        iterator.remove();
                    }
                }

                if (LogstashWrapperBranchPropertyDefinition.this.parameterDefinitions != null && !LogstashWrapperBranchPropertyDefinition.this.parameterDefinitions.isEmpty()) {
                    result.add(new ParametersDefinitionProperty(LogstashWrapperBranchPropertyDefinition.this.parameterDefinitions));
                }

                result.add((JobProperty<? super P>) new LogstashLoggerDecoratorJobProperty());

                return result;
            }
        };
    }

    @DataBoundConstructor
    public LogstashWrapperBranchPropertyDefinition() {
    }

    @Symbol("sendToLostash")
    @Extension
    public static class DescriptorImpl extends BranchPropertyDescriptor {
        @Override
        protected boolean isApplicable(MultiBranchProjectDescriptor projectDescriptor) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Send console log to Logstash";
        }
    }
}

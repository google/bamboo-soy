package com.google.bamboo.soy;

import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.ConfigBuilder;
import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RollbarErrorReportSubmitter extends ErrorReportSubmitter {

  private static final String REPORT_ACTION_TEXT = "Submit error to plugin maintainer";
  private static final String DEFAULT_RESPONSE = "Thank you for your report.";
  private static final String DEFAULT_RESPONSE_TITLE = "Report Submitted";

  private static final String EXTRA_MORE_EVENTS = "More Events";
  private static final String EXTRA_ADDITIONAL_INFO = "Additional Info";

  private static final String EVENT_MESSAGE_FORMAT = "<event-message>%s</event-message>";

  private static final String TAG_PLATFORM_VERSION = "platform";
  private static final String TAG_OS = "os";
  private static final String TAG_OS_VERSION = "os_version";
  private static final String TAG_OS_ARCH = "os_arch";
  private static final String TAG_JAVA_VERSION = "java_version";
  private static final String TAG_JAVA_RUNTIME_VERSION = "java_runtime_version";

  private static final String ACCESS_TOKEN = "38a26ea62cbe431297e05b8943ab56cc";

  private static final String PLUGIN_ID = "com.google.bamboo.id";

  private static final Rollbar rollbar = new Rollbar(
      ConfigBuilder.withAccessToken(ACCESS_TOKEN)
          .environment("production")
          .codeVersion(getPluginVersion())
          .build());

  private static String getPluginVersion() {
    IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID));
    if (plugin == null) {
      return "unknown";
    }
    return plugin.getVersion();
  }

  @NotNull
  @Override
  public String getReportActionText() {
    return REPORT_ACTION_TEXT;
  }

  @Override
  public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo,
      @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
    log(events, StringUtil.notNullize(additionalInfo));
    consumer.consume(new SubmittedReportInfo(null, null, NEW_ISSUE));
    Messages.showInfoMessage(parentComponent, DEFAULT_RESPONSE, DEFAULT_RESPONSE_TITLE);
    return true;
  }

  private void log(@NotNull IdeaLoggingEvent[] events, @NotNull String additionalInfo) {
    IdeaLoggingEvent ideaEvent = events[0];
    if (ideaEvent.getThrowableText().isEmpty()) {
      return;
    }
    LinkedHashMap<String, Object> customData = new LinkedHashMap<>();
    customData.put(TAG_PLATFORM_VERSION, ApplicationInfo.getInstance().getBuild().asString());
    customData.put(TAG_OS, SystemInfo.OS_NAME);
    customData.put(TAG_OS_VERSION, SystemInfo.OS_VERSION);
    customData.put(TAG_OS_ARCH, SystemInfo.OS_ARCH);
    customData.put(TAG_JAVA_VERSION, SystemInfo.JAVA_VERSION);
    customData.put(TAG_JAVA_RUNTIME_VERSION, SystemInfo.JAVA_RUNTIME_VERSION);
    if (!StringUtil.isEmptyOrSpaces(ideaEvent.getMessage())) {
      additionalInfo += String.format(EVENT_MESSAGE_FORMAT, ideaEvent.getMessage());
    }
    if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
      customData.put(EXTRA_ADDITIONAL_INFO, additionalInfo);
    }
    if (events.length > 1) {
      customData.put(EXTRA_MORE_EVENTS,
          Stream.of(events).map(Object::toString).collect(Collectors.joining("\n")));
    }
    rollbar.error(ideaEvent.getThrowableText(), customData);
  }
}

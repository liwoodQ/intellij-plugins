package org.intellij.plugins.markdown.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MarkdownSettingsConfigurable implements SearchableConfigurable {
  static final String PLANT_UML_DIRECTORY = "plantUML";
  static final String PLANTUML_JAR_URL =
    "http://central.maven.org/maven2/net/sourceforge/plantuml/plantuml/6703/plantuml-6703.jar";
  static final String PLANTUML_JAR = "plantuml.jar";

  private static final String DOWNLOAD_CACHE_DIRECTORY = "download-cache";
  @Nullable
  private MarkdownSettingsForm myForm = null;
  @NotNull
  private final MarkdownApplicationSettings myMarkdownApplicationSettings;

  public MarkdownSettingsConfigurable(@NotNull MarkdownApplicationSettings markdownApplicationSettings) {
    myMarkdownApplicationSettings = markdownApplicationSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return "Settings.Markdown";
  }

  @Nls
  @Override
  public String getDisplayName() {
    return MarkdownBundle.message("markdown.settings.name");
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    MarkdownSettingsForm form = getForm();
    if (form == null) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(MarkdownBundle.message("markdown.settings.no.providers")), BorderLayout.NORTH);
      return panel;
    }
    return form.getComponent();
  }

  @Nullable
  public MarkdownSettingsForm getForm() {
    if (!MarkdownHtmlPanelProvider.hasAvailableProviders()) {
      return null;
    }

    if (myForm == null) {
      myForm = new MarkdownSettingsForm();
    }
    return myForm;
  }

  @Override
  public boolean isModified() {
    MarkdownSettingsForm form = getForm();
    if (form == null) {
      return false;
    }
    return !form.getMarkdownCssSettings().equals(myMarkdownApplicationSettings.getMarkdownCssSettings()) ||
           !form.getMarkdownPreviewSettings().equals(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    final MarkdownSettingsForm form = getForm();
    if (form == null) {
      return;
    }

    form.validate();

    myMarkdownApplicationSettings.setMarkdownCssSettings(form.getMarkdownCssSettings());
    myMarkdownApplicationSettings.setMarkdownPreviewSettings(form.getMarkdownPreviewSettings());

    ApplicationManager.getApplication().getMessageBus().syncPublisher(MarkdownApplicationSettings.SettingsChangedListener.TOPIC)
      .settingsChanged(myMarkdownApplicationSettings);
  }

  @Override
  public void reset() {
    MarkdownSettingsForm form = getForm();
    if (form == null) {
      return;
    }
    form.setMarkdownCssSettings(myMarkdownApplicationSettings.getMarkdownCssSettings());
    form.setMarkdownPreviewSettings(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void disposeUIResources() {
    if (myForm != null) {
      Disposer.dispose(myForm);
    }
    myForm = null;
  }

  /**
   * Returns true if PlantUML jar has been already downloaded
   */
  public static boolean isPlantUMLAvailable() {
    return getDownloadedJarPath().exists();
  }

  /**
   * Gets 'download-cache' directory PlantUML jar to be download to
   */
  @NotNull
  public static File getDirectoryToDownload() {
    return new File(PathManager.getSystemPath(), DOWNLOAD_CACHE_DIRECTORY + "/" + PLANT_UML_DIRECTORY);
  }

  /**
   * Returns {@link File} presentation of downloaded PlantUML jar
   */
  @NotNull
  public static File getDownloadedJarPath() {
    return new File(getDirectoryToDownload(), PLANTUML_JAR);
  }
}

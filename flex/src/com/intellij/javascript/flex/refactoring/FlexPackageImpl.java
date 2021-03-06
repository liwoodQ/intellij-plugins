/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.javascript.flex.refactoring;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSNamespace;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiPackageBase;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class FlexPackageImpl extends PsiPackageBase implements NavigationItem, JSQualifiedNamedElement {

  private volatile CachedValue<Collection<PsiDirectory>> myDirectories;

  public FlexPackageImpl(PsiManager manager, String qualifiedName) {
    super(manager, qualifiedName);
  }

  @Override
  protected FlexPackageImpl findPackage(String qName) {
    return new FlexPackageImpl(getManager(), qName);
  }

  @Override
  protected Collection<PsiDirectory> getAllDirectories(boolean includeLibrarySources) {
    if (myDirectories == null) {
      CachedValueProvider<Collection<PsiDirectory>> provider = () -> {
        final PsiManager manager = PsiManager.getInstance(getProject());
        Collection<PsiDirectory> directories = ContainerUtil.map(DirectoryIndex.getInstance(getProject())
                                                                   .getDirectoriesByPackageName(getQualifiedName(), true).findAll(),
                                                                 virtualFile -> manager.findDirectory(virtualFile));

        return CachedValueProvider.Result.create(directories,
                                                 PsiModificationTracker.MODIFICATION_COUNT,
                                                 ProjectRootManager
                               .getInstance(getProject()));
      };
      myDirectories =
        CachedValuesManager.getManager(getProject()).createCachedValue(provider, false);
    }
    return myDirectories.getValue();
  }

  @Override
  public boolean isValid() {
    return getDirectories().length > 0;
  }

  @Override
  @NotNull
  public Language getLanguage() {
    return StdFileTypes.JS.getLanguage();
  }

  public String toString() {
    return "Package:" + getQualifiedName();
  }

  @Override
  public void navigate(final boolean requestFocus) {
    // TODO
    //ToolWindow window = ToolWindowManager.getInstance(getProject()).getToolWindow(ToolWindowId.PROJECT_VIEW);
    //window.activate(null);
    //window.getActivation().doWhenDone(new Runnable() {
    //  public void run() {
    //    final ProjectView projectView = ProjectView.getInstance(getProject());
    //    projectView.changeView(PackageViewPane.ID);
    //    final PsiDirectory[] directories = getDirectories();
    //    final VirtualFile firstDir = directories[0].getVirtualFile();
    //    final boolean isLibraryRoot = ProjectRootsUtil.isLibraryRoot(firstDir, getProject());
    //
    //    final Module module = ProjectRootManager.getInstance(getProject()).getFileIndex().getModuleForFile(firstDir);
    //    final PackageElement packageElement = new PackageElement(module, PsiPackageImpl.this, isLibraryRoot);
    //    projectView.getProjectViewPaneById(PackageViewPane.ID).select(packageElement, firstDir, requestFocus);
    //  }
    //});
  }

  @Override
  public ASTNode findNameIdentifier() {
    return null;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return null;
  }

  @NotNull
  @Override
  public JSContext getJSContext() {
    return JSContext.UNKNOWN;
  }

  @Nullable
  @Override
  public JSQualifiedName getNamespace() {
    return JSPsiImplUtils.buildNamespaceFromQualifiedName(this);
  }

  @Override
  public boolean isNamespaceExplicitlyDeclared() {
    return true;
  }

  @NotNull
  @Override
  public JSAttributeList.AccessType getAccessType() {
    return JSAttributeList.AccessType.PACKAGE_LOCAL;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @NotNull
  @Override
  public ClassOrInterface isClassOrInterface() {
    return ClassOrInterface.NONE;
  }

  @NotNull
  @Override
  public JSNamespace getJSNamespace() {
    return JSNamedTypeFactory.buildJSNamespace(this);
  }
}

package com.leandrosnazareth.base.ui.view;

import com.leandrosnazareth.security.CurrentUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll
@CssImport("./styles/sidebar.css")
public final class MainLayout extends AppLayout {

    private final CurrentUser currentUser;
    private final AuthenticationContext authenticationContext;

    MainLayout(CurrentUser currentUser, AuthenticationContext authenticationContext) {
        this.currentUser = currentUser;
        this.authenticationContext = authenticationContext;
        setPrimarySection(Section.DRAWER);
        addToDrawer(createHeader(), new Scroller(createSideNav()), createUserMenu());
    }

    private Div createHeader() {
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);
        appLogo.addClassName("sidebar-logo");

        var appName = new Span("💼 Sistema PDV");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);
        appName.addClassName("sidebar-title");
        appName.getStyle().set("color", "gray !important");

        var header = new Div(appLogo, appName);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
        header.addClassName("sidebar-header");
        
        // Adicionar efeito de hover interativo
        header.getElement().addEventListener("click", e -> {
            appLogo.getElement().executeJs("this.style.transform = 'scale(1.2) rotate(360deg)'; setTimeout(() => this.style.transform = '', 500);");
        });
        
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM, Margin.Vertical.SMALL);
        MenuConfiguration.getMenuEntries().forEach(entry -> {
            var item = createSideNavItem(entry);
            nav.addItem(item);
        });
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        SideNavItem item;
        if (menuEntry.icon() != null) {
            var icon = new Icon(menuEntry.icon());
            icon.getStyle().set("margin-right", "var(--lumo-space-s)");
            item = new SideNavItem(menuEntry.title(), menuEntry.path(), icon);
        } else {
            item = new SideNavItem(menuEntry.title(), menuEntry.path());
        }
        
        // Adicionar classes para melhor styling
        item.addClassName("nav-item");
        
        return item;
    }

    private Component createUserMenu() {
        var user = currentUser.require();

        var avatar = new Avatar(user.getFullName(), user.getPictureUrl());
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(Margin.Right.SMALL);
        avatar.setColorIndex(5);

        var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(Margin.MEDIUM);

        var userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add(user.getFullName());
        if (user.getProfileUrl() != null) {
            userMenuItem.getSubMenu().addItem("View Profile",
                    event -> UI.getCurrent().getPage().open(user.getProfileUrl()));
        }
        userMenuItem.getSubMenu().addItem("Logout", event -> authenticationContext.logout());

        return userMenu;
    }

}

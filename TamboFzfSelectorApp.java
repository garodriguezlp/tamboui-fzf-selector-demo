/// usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 17

//COMPILE_OPTIONS -encoding UTF-8
//RUNTIME_OPTIONS -Dfile.encoding=UTF-8

//REPOS central

//DEPS dev.tamboui:tamboui-toolkit:LATEST
//DEPS dev.tamboui:tamboui-jline3-backend:LATEST

import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.dock;
import static dev.tamboui.toolkit.Toolkit.gauge;
import static dev.tamboui.toolkit.Toolkit.list;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spinner;
import static dev.tamboui.toolkit.Toolkit.text;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.tamboui.style.Color;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.bindings.ActionHandler;
import dev.tamboui.tui.bindings.Actions;
import dev.tamboui.tui.bindings.BindingSets;
import dev.tamboui.tui.bindings.Bindings;
import dev.tamboui.tui.bindings.KeyTrigger;
import dev.tamboui.tui.event.Event;
import dev.tamboui.tui.event.KeyCode;
import dev.tamboui.tui.event.KeyEvent;

public class TamboFzfSelectorApp extends ToolkitApp {

    private final FzfModel model;
    private final FzfController controller;
    private final FzfInputHandler inputHandler;
    private final FzfView view;

    private TamboFzfSelectorApp() {
        this.model = new FzfModel();
        this.controller = new FzfController(model, this::quit);
        this.inputHandler = new FzfInputHandler(controller);
        this.view = new FzfView(model, inputHandler);
    }

    public static void main(String[] args) throws Exception {
        var app = new TamboFzfSelectorApp();
        app.run();

        if (app.model.selectedItem() != null) {
            System.out.println(app.model.selectedItem());
            System.exit(0);
        }
        System.exit(1);
    }

    @Override
    protected void onStart() {
        runner().scheduleRepeating(() -> {
            runner().runOnRenderThread(() -> {
                if (model.loadPhase() == FzfModel.LoadPhase.LOADING) {
                    model.advanceLoad(0.05);
                }
            });
        }, Duration.ofMillis(50));
    }

    @Override
    protected Element render() {
        return switch (model.loadPhase()) {
            case LOADING -> view.renderLoading();
            case READY -> view.render();
        };
    }
}

final class FzfModel {

    enum LoadPhase { LOADING, READY }

    private final List<String> allItems = new ArrayList<>();
    private final List<String> filteredItems = new ArrayList<>();

    private String filterText = "";
    private int selectedIndex = 0;
    private String selectedItem = null;

    private LoadPhase loadPhase = LoadPhase.LOADING;
    private double loadProgress = 0.0;

    LoadPhase loadPhase() {
        return loadPhase;
    }

    double loadProgress() {
        return loadProgress;
    }

    void advanceLoad(double delta) {
        loadProgress = Math.min(1.0, loadProgress + delta);
        if (loadProgress >= 1.0) {
            loadHardcodedItems();
            rebuildAndClampSelection();
            loadPhase = LoadPhase.READY;
        }
    }

    List<String> filteredItems() {
        return List.copyOf(filteredItems);
    }

    int selectedIndex() {
        return selectedIndex;
    }

    String filterText() {
        return filterText;
    }

    String selectedItem() {
        return selectedItem;
    }

    int resultCount() {
        return filteredItems.size();
    }

    boolean hasFilter() {
        return !filterText.isEmpty();
    }

    boolean hasResults() {
        return !filteredItems.isEmpty();
    }

    void moveUp() {
        if (!hasResults()) {
            selectedIndex = 0;
            return;
        }
        selectedIndex = Math.max(0, selectedIndex - 1);
    }

    void moveDown() {
        if (!hasResults()) {
            selectedIndex = 0;
            return;
        }
        selectedIndex = Math.min(filteredItems.size() - 1, selectedIndex + 1);
    }

    void appendFilterCharacter(char c) {
        filterText += c;
        rebuildAndClampSelection();
    }

    void deleteFilterCharacter() {
        if (!hasFilter()) {
            return;
        }
        filterText = filterText.substring(0, filterText.length() - 1);
        rebuildAndClampSelection();
    }

    void clearFilter() {
        filterText = "";
        rebuildAndClampSelection();
    }

    boolean confirmSelection() {
        if (!hasResults()) {
            return false;
        }
        selectedItem = filteredItems.get(selectedIndex);
        return true;
    }

    List<String> displayLines() {
        if (!hasResults()) {
            return List.of("No matches");
        }
        return filteredItems();
    }

    private void rebuildAndClampSelection() {
        rebuildFilteredItems();
        clampSelectedIndex();
    }

    private void clampSelectedIndex() {
        if (filteredItems.isEmpty()) {
            selectedIndex = 0;
            return;
        }
        selectedIndex = Math.max(0, Math.min(selectedIndex, filteredItems.size() - 1));
    }

    private void rebuildFilteredItems() {
        filteredItems.clear();
        var filter = filterText.toLowerCase(Locale.ROOT);

        for (var item : allItems) {
            if (filter.isEmpty() || item.toLowerCase(Locale.ROOT).contains(filter)) {
                filteredItems.add(item);
            }
        }
    }

    private void loadHardcodedItems() {
        allItems.clear();

        allItems.add("apple");
        allItems.add("apricot");
        allItems.add("banana");
        allItems.add("blackberry");
        allItems.add("blueberry");
        allItems.add("clementine");
        allItems.add("cranberry");
        allItems.add("dragonfruit");
        allItems.add("fig");
        allItems.add("grape");
        allItems.add("grapefruit");
        allItems.add("guava");
        allItems.add("kiwi");
        allItems.add("lemon");
        allItems.add("lime");
        allItems.add("mango");
        allItems.add("nectarine");
        allItems.add("orange");
        allItems.add("papaya");
        allItems.add("peach");
        allItems.add("pear");
        allItems.add("pineapple");
        allItems.add("plum");
        allItems.add("pomegranate");
        allItems.add("raspberry");
        allItems.add("strawberry");
        allItems.add("tangerine");
        allItems.add("watermelon");
    }
}

final class FzfController {

    private final FzfModel model;
    private final Runnable quitAction;

    FzfController(FzfModel model, Runnable quitAction) {
        this.model = model;
        this.quitAction = quitAction;
    }

    void moveUp() {
        model.moveUp();
    }

    void moveDown() {
        model.moveDown();
    }

    boolean select() {
        if (!model.confirmSelection()) {
            return false;
        }
        quitAction.run();
        return true;
    }

    boolean clearFilter() {
        if (!model.hasFilter()) {
            return false;
        }
        model.clearFilter();
        return true;
    }

    boolean deleteFilterCharacter() {
        if (!model.hasFilter()) {
            return false;
        }
        model.deleteFilterCharacter();
        return true;
    }

    void appendFilterCharacter(char c) {
        model.appendFilterCharacter(c);
    }
}

final class FzfInputHandler {

    private static final Bindings BINDINGS = BindingSets.defaults().toBuilder()
            .bind(KeyTrigger.key(KeyCode.CHAR), "appendFilterCharacter")
            .rebind(KeyTrigger.key(KeyCode.ENTER), Actions.SELECT)
            .rebind(KeyTrigger.ctrl('h'), Actions.DELETE_BACKWARD)
            .build();

    private final FzfController controller;
    private final ActionHandler actionHandler;

    FzfInputHandler(FzfController controller) {
        this.controller = controller;
        this.actionHandler = new ActionHandler(BINDINGS)
                .on(Actions.MOVE_UP, this::handleMoveUp)
                .on(Actions.MOVE_DOWN, this::handleMoveDown)
                .on("appendFilterCharacter", this::handleAppendFilterCharacter)
                .on(Actions.SELECT, this::handleSelect)
                .on(Actions.CANCEL, this::handleClearFilter)
                .on(Actions.DELETE_BACKWARD, this::handleDeleteFilterCharacter);
    }

    EventResult handle(KeyEvent event) {
        return actionHandler.dispatch(event) ? EventResult.HANDLED : EventResult.UNHANDLED;
    }

    private void handleMoveUp(Event event) {
        controller.moveUp();
    }

    private void handleMoveDown(Event event) {
        controller.moveDown();
    }

    private void handleAppendFilterCharacter(Event event) {
        if (event instanceof KeyEvent keyEvent) {
            controller.appendFilterCharacter(keyEvent.character());
        }
    }

    private void handleDeleteFilterCharacter(Event event) {
        controller.deleteFilterCharacter();
    }

    private void handleSelect(Event event) {
        controller.select();
    }

    private void handleClearFilter(Event event) {
        controller.clearFilter();
    }
}

final class FzfView {

    private final FzfModel model;
    private final FzfInputHandler inputHandler;

    FzfView(FzfModel model, FzfInputHandler inputHandler) {
        this.model = model;
        this.inputHandler = inputHandler;
    }

    Element render() {
        return dock()
                .top(headerPanel())
                .center(row(listPanel(), detailPanel()))
                .bottom(column(filterPanel(), footerPanel()));
    }

    Element renderLoading() {
        return dock()
                .top(headerPanel())
                .center(row(loadingListPanel(), detailPanel()))
                .bottom(loadingFooterPanel());
    }

    private Element loadingListPanel() {
        return panel(
                column(
                        row(spinner().cyan(), text("  Fetching items from API\u2026").dim()),
                        gauge(model.loadProgress()).green()
                )
        ).rounded().borderColor(Color.CYAN).title("Loading\u2026").fill(1);
    }

    private Element loadingFooterPanel() {
        return panel(
                text("Loading data, please wait\u2026").dim()
        ).rounded().borderColor(Color.DARK_GRAY);
    }

    private Element headerPanel() {
        return panel(
                column(
                        text(" FZF-like Selector ").bold().cyan(),
                        row(
                                text("Filter: ").dim(),
                                text(model.filterText().isEmpty() ? "(empty)" : model.filterText()).yellow(),
                                text("   Matches: ").dim(),
                                text(String.valueOf(model.resultCount())).bold().green()
                        )
                )
        ).rounded().borderColor(Color.CYAN);
    }

    private Element listPanel() {
        var listWidget = list()
                .highlightSymbol("> ")
                .highlightColor(Color.YELLOW)
                .autoScroll()
                .scrollbar()
                .scrollbarThumbColor(Color.CYAN)
                .items(model.displayLines())
                .selected(model.selectedIndex());

        return panel(listWidget)
                .rounded()
        .borderColor(listBorderColor())
                .id("fzf-list")
                .focusedBorderColor(Color.YELLOW)
                .focusable()
                .onKeyEvent(inputHandler::handle)
                .fill(1);
    }

    private Element filterPanel() {
        return panel(
                row(
                        text("> ").bold().yellow(),
                        text(model.filterText().isEmpty() ? "" : model.filterText()).yellow(),
                        text("  [" + model.resultCount() + " matches]").dim()
                )
        ).rounded().borderColor(listBorderColor()).title(filterTitle());
    }

    private Element detailPanel() {
        var items = model.filteredItems();
        var index = model.selectedIndex();
        var content = (items.isEmpty() || index < 0 || index >= items.size())
                ? text("No selection").dim()
                : column(
                text("Selected Item").bold().cyan(),
                text(items.get(index)).bold().yellow()
        );
        return panel(content)
                .title("Details")
                .rounded()
                .borderColor(Color.CYAN)
                .fill(1);
    }

    private Element footerPanel() {
        return panel(
                text("Type to filter | Up/Down: move | Enter: select | Esc: clear filter | Ctrl+C/q: quit").dim()
        ).rounded().borderColor(Color.DARK_GRAY);
    }

    private Color listBorderColor() {
        return model.hasFilter() ? Color.YELLOW : Color.CYAN;
    }

    private String filterTitle() {
        return model.hasFilter() ? "Filter: " + model.filterText() : "Type to filter";
    }
}
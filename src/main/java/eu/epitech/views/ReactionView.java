package eu.epitech.views;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import eu.epitech.API.ApiUtils;
import eu.epitech.NavigatorUI;
import eu.epitech.Stock;
import eu.epitech.User;
import eu.epitech.action.AAction;
import eu.epitech.reaction.AReaction;

import java.util.ArrayList;

/**
 * !! DO NOT EDIT THIS FILE !!
 * <p>
 * This class is generated by Vaadin Designer and will be overwritten.
 * <p>
 * Please make a subclass with logic and additional interfaces as needed,
 * e.g class LoginView extends LoginDesign implements View { }
 */
@DesignRoot
@AutoGenerated
@SuppressWarnings("serial")
public class ReactionView extends AbsoluteLayout implements View {
    private ArrayList<Button> reactionsButton = new ArrayList<>();
    private String titleAction;
    private User user = null;
    private AAction action = null;

    /*
    *  With the action passing in this view, set in the ReactionView constructor the nb of reaction linked
    *  to the actual action.
     */
    public ReactionView() {
        setSizeFull();
        int maxHeight = (ApiUtils.availableReactions.size() + 10) * 100;
        setWidth("1000px");
        setHeight(Integer.toString(maxHeight) + "px");
        for (AReaction reaction : ApiUtils.availableReactions) {
            reactionsButton.add(reactionButton(reaction.getName()));
        }
    }

    /*
    * Here we're setting the big action button corresponding to the txt passing in parameters to this view
     */
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        try {
            Stock stock = (Stock) NavigatorUI.readData(getUI());
            if (stock != null) {
                if (stock.getPrompt() != null) {
                    addComponent(new Label(stock.getPrompt()));
                }
                if (stock.getUser() != null) {
                    this.user = stock.getUser();
                } else {
                    NavigatorUI.putData(getUI(), new Stock(null, null, null, "You are not connected"));
                    getUI().getNavigator().navigateTo("");
                }
                if (stock.getAction() != null) {
                    this.action = stock.getAction();
                } else {
                    NavigatorUI.putData(getUI(), new Stock(this.user, null, null, "You must choose an action first"));
                    getUI().getNavigator().navigateTo("action");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (event.getParameters() != null) {
            titleAction = event.getParameters();
            addComponent(actionButton(titleAction), "top: 50px; left: 50px;");
        } else {
            NavigatorUI.putData(getUI(), new Stock(null, null, null, "You must choose an action first"));
            getUI().getNavigator().navigateTo("action");
        }

        int high = 50;
        for (int i = 0; i < ApiUtils.availableReactions.size(); i++) {
            if (ApiUtils.availableReactions.get(i).isExecutable(this.action.getFields())) {
                addComponent(reactionsButton.get(i), "top: " + Integer.toString(high) + "px; left: 400px;");
                high += 100;
            }
        }
    }

    private Button actionButton(String txt) {
        Button button = new Button(txt, (Button.ClickListener) clickEvent -> {
        });

        button.setWidth("300");
        button.setHeight("500");
        button.setResponsive(true);
        return button;
    }

    /*
    ** The String txt correspond to the name of the reaction. You can add a second parameters which will can be
    * the description of the reaction. In this case, append this parameters to 'txt' into the Button constructor.
     */
    private Button reactionButton(String txt) {
        Button button = new Button(txt, (Button.ClickListener) clickEvent -> {
            /*  We're passing in parameters to the next view the action linked to the reaction.
            *   Here we can pass the specific object Config with the specific methods situated in NavigatorView
            *   to the next view.
            */
            NavigatorUI.putData(getUI(), new Stock(user, action, ApiUtils.createReactionFromName(txt), null));
            getUI().getNavigator().navigateTo("config" + "/" + titleAction + "-" + txt);
        });

        button.setWidth("300");
        button.setHeight("100");
        button.setResponsive(true);
        return button;
    }
}

package org.yes.cart.web.page;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.yes.cart.constants.ServiceSpringKeys;
import org.yes.cart.domain.entity.Address;
import org.yes.cart.domain.entity.Customer;
import org.yes.cart.service.domain.CustomerService;
import org.yes.cart.web.application.ApplicationDirector;
import org.yes.cart.web.page.component.customer.address.ManageAddressesView;
import org.yes.cart.web.page.component.customer.dynaform.DynaFormPanel;
import org.yes.cart.web.page.component.customer.order.CustomerOrderPanel;
import org.yes.cart.web.page.component.customer.password.PasswordPanel;
import org.yes.cart.web.page.component.customer.summary.SummaryPanel;

/**
 *
 * Customer self care page to view orders, wish list, etc.
 *
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 10/11/11
 * Time: 9:51 PM
 */
@AuthorizeInstantiation("USER")
public class CustomerSelfCarePage extends AbstractWebPage {

    // ------------------------------------- MARKUP IDs BEGIN ---------------------------------- //
    private final static String SUMMARY_PANEL = "summaryView";
    private final static String ATTR_PANEL = "attributesView";
    private final static String PASSWORD_PANEL = "passwordView";
    private final static String SHIPPING_ADDR_PANEL = "shippingAddressesView";
    private final static String BILLING_ADDR_PANEL = "billingAddressesView";
    private final static String ORDERS_PANEL = "ordersView";
    // ------------------------------------- MARKUP IDs END ---------------------------------- //

    @SpringBean(name = ServiceSpringKeys.CUSTOMER_SERVICE)
    private CustomerService customerService;


    /**
     * Construct page.
     *
     * @param params page parameters
     */
    public CustomerSelfCarePage(final PageParameters params) {
        super(params);

        final Customer customer = customerService.findCustomer(ApplicationDirector.getShoppingCart().getCustomerEmail());

        final Model<Customer> customerModel = new Model<Customer>(customer);


        add(
                new FeedbackPanel(FEEDBACK)
        ).add(
                new PasswordPanel(PASSWORD_PANEL, customerModel)
        ).add(
                new ManageAddressesView(SHIPPING_ADDR_PANEL, customerModel, Address.ADDR_TYPE_SHIPING, false)
        ).add(
                new ManageAddressesView(BILLING_ADDR_PANEL, customerModel, Address.ADDR_TYPE_BILLING, false)
        ).add(
                new DynaFormPanel(ATTR_PANEL, customerModel)
        ).add(
                new SummaryPanel(SUMMARY_PANEL, customerModel)
        ).add(
                new CustomerOrderPanel(ORDERS_PANEL, customerModel)
        );

    }


}

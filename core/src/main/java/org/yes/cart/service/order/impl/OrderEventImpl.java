package org.yes.cart.service.order.impl;

import org.yes.cart.domain.entity.CustomerOrder;
import org.yes.cart.domain.entity.CustomerOrderDelivery;
import org.yes.cart.service.order.OrderEvent;

import java.util.Map;

/**
 *
 * Event with id and his context. One of the context element is order.
 *
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class OrderEventImpl  implements OrderEvent {

    private final String eventId;
    private final CustomerOrder customerOrder;
    private final CustomerOrderDelivery customerOrderDelivery;
    private final Map params;

    /**
     * Construct order event.
     * @param eventId trigger name
     * @param customerOrder order
     */
    public OrderEventImpl(final String eventId,
                          final CustomerOrder customerOrder) {
        this(eventId, customerOrder, null, null);

    }

    /**
     * Construct order event.
     * @param eventId trigger name
     * @param customerOrder order
     * @param customerOrderDelivery optional delivery
     */
    public OrderEventImpl(final String eventId,
                          final CustomerOrder customerOrder,
                          final CustomerOrderDelivery customerOrderDelivery) {
        this(eventId, customerOrder, customerOrderDelivery, null);
    }

    /**
     * Construct order event.
     * @param eventId trigger name
     * @param customerOrder order
     * @param customerOrderDelivery optional delivery
     * @param params optional params
     */
    public OrderEventImpl(final String eventId,
                          final CustomerOrder customerOrder,
                          final CustomerOrderDelivery customerOrderDelivery,
                          final Map params) {
        this.eventId = eventId;
        this.customerOrder = customerOrder;
        this.customerOrderDelivery = customerOrderDelivery;
        this.params = params;
    }

    /** {@inheritDoc}*/
    public String getEventId() {
        return eventId;
    }

    /** {@inheritDoc}*/
    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    /** {@inheritDoc}*/
    public CustomerOrderDelivery getCustomerOrderDelivery() {
        return customerOrderDelivery;
    }

    /** {@inheritDoc}*/
    public Map getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "OrderEventImpl{"
                + "eventId='" + eventId + '\''
                + ", customerOrder=" +  (customerOrder == null ? "" : customerOrder.getOrdernum())
                + " order status " + (customerOrder == null ? "" : customerOrder.getOrderStatus() )
                + (customerOrderDelivery==null?"":", customerOrderDelivery=" + customerOrderDelivery.getDevileryNum() + " delivery status = " + customerOrderDelivery.getDeliveryStatus()) +
                '}';
    }
}

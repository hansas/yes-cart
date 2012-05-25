package org.yes.cart.domain.dto.impl;

import dp.lib.dto.geda.annotations.Dto;
import dp.lib.dto.geda.annotations.DtoField;
import org.yes.cart.domain.dto.ShopUrlDTO;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
@Dto
public class ShopUrlDTOImpl implements ShopUrlDTO {

    private static final long serialVersionUID = 20100528L;

    @DtoField(value = "url")
    private String url;

    @DtoField(value = "storeUrlId", readOnly = true)
    private long storeUrlId;

    @DtoField(value = "shop.shopId", readOnly = true)
    private long shopId;

    /** {@inheritDoc}  */
    public String getUrl() {
        return url;
    }

    /** {@inheritDoc}  */
    public void setUrl(final String url) {
        this.url = url;
    }

    /** {@inheritDoc}  */
    public long getStoreUrlId() {
        return storeUrlId;
    }

    /** {@inheritDoc}*/
    public long getId() {
        return storeUrlId;
    }

    /** {@inheritDoc}  */
    public void setStoreUrlId(final long storeUrlId) {
        this.storeUrlId = storeUrlId;
    }

    public long getShopId() {
        return shopId;
    }

    /** {@inheritDoc}  */
    public void setShopId(final long shopId) {
        this.shopId = shopId;
    }
}
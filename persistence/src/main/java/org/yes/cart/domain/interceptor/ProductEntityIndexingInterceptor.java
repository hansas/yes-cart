/*
 * Copyright 2009 Igor Azarnyi, Denys Pavlov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.domain.interceptor;

import org.hibernate.search.indexes.interceptor.EntityIndexingInterceptor;
import org.hibernate.search.indexes.interceptor.IndexingOverride;
import org.yes.cart.domain.entity.Product;
import org.yes.cart.domain.entity.ProductSku;
import org.yes.cart.domain.entity.SkuWarehouse;

import java.util.Date;

/**
 * User:  Igor Azarny
 * Date: 5/5/12
 * Time: 3:40 PM
 */
public class ProductEntityIndexingInterceptor implements EntityIndexingInterceptor<Product> {


    /**
     * Check is product need to be in index.
     * Product will be added to index:
     * in case if product available for pre/back order;
     * or always available (for example digital products);
     * or has quantity of sku more than 0  on any stock (in any shop).
     *
     *
     * @param entity entity to check
     * @return true if entity need to be in lucene index.
     */
    public boolean isIncludeInLuceneIndex(final Product entity) {
        if (entity != null) {
            if (entity.getProductCategory().isEmpty()) {
                return false; // if it is not assigned to category, no way to determine the shop
            }
            if (entity.getAvailableto() == null || (entity.getAvailableto() != null && entity.getAvailableto().after(new Date()))) {
               if (Product.AVAILABILITY_STANDARD == entity.getAvailability()) {
                   for (final ProductSku sku : entity.getSku()) {
                       for (final SkuWarehouse inventory : sku.getQuantityOnWarehouse()) {
                           if (inventory.isAvailableToSell()) {
                               return true; // has stock
                           }
                       }
                   }
                   return false; // no
               }
               return true;
            }
        }
        return false;
    }


    /** {@inheritDoc} */
    public IndexingOverride onAdd(final Product entity) {
        return isIncludeInLuceneIndex(entity)
                ?IndexingOverride.APPLY_DEFAULT
                :IndexingOverride.REMOVE;
    }

    /** {@inheritDoc} */
    public IndexingOverride onUpdate(final Product entity) {
        return isIncludeInLuceneIndex(entity)
                ?IndexingOverride.APPLY_DEFAULT
                :IndexingOverride.REMOVE;
    }

    /** {@inheritDoc} */
    public IndexingOverride onDelete(final Product entity) {
        return IndexingOverride.APPLY_DEFAULT;
    }

    /** {@inheritDoc} */
    public IndexingOverride onCollectionUpdate(final Product entity) {
        return isIncludeInLuceneIndex(entity)
                ?IndexingOverride.APPLY_DEFAULT
                :IndexingOverride.REMOVE;
    }

}

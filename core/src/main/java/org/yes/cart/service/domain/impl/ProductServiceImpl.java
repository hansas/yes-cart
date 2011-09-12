package org.yes.cart.service.domain.impl;

import org.apache.lucene.search.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.util.CollectionUtils;
import org.yes.cart.cache.Cacheable;
import org.yes.cart.dao.GenericDAO;
import org.yes.cart.domain.entity.*;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.domain.misc.navigation.range.RangeList;
import org.yes.cart.domain.misc.navigation.range.RangeNode;
import org.yes.cart.domain.queryobject.FiteredNavigationRecord;
import org.yes.cart.domain.queryobject.impl.FiteredNavigationRecordImpl;
import org.yes.cart.service.domain.ProductService;

import java.math.BigDecimal;
import java.util.*;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 09-May-2011
 * Time: 14:12:54
 */
public class ProductServiceImpl extends BaseGenericServiceImpl<Product> implements ProductService {

    private final static String PROD_SERV_METHOD_CACHE =  "productServiceImplMethodCache";

    private final GenericDAO<Product, Long> productDao;
    private final GenericDAO<ProductSku, Long> productSkuDao;
    private final GenericDAO<ProductType, Long> productTypeDao;
    private final Random rand;

    public ProductServiceImpl(final GenericDAO<Product, Long> productDao,
                              final GenericDAO<ProductSku, Long> productSkuDao,
                              final GenericDAO<ProductType, Long> productTypeDao) {
        super(productDao);
        this.productDao = productDao;
        this.productSkuDao = productSkuDao;
        this.productTypeDao = productTypeDao;
        rand = new Random();
        rand.setSeed((new Date().getTime()));
    }

    /**
     * Persist product. Default sku will be created.
     * @param instance instance to persist
     * @return persisted instanse
     */
    public Product create(final Product instance) {

        ProductSku sku = productDao.getEntityFactory().getByIface(ProductSku.class);
        sku.setCode(instance.getCode());
        sku.setName(instance.getName());
        sku.setDescription(instance.getDescription());
        sku.setProduct(instance);
        instance.getSku().add(sku);

        return getGenericDao().create(instance);
    }

    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public Product getById(final Long productId) {
        return productDao.findById(productId);
    }

    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public ProductSku getSkuById(final Long skuId) {
        return productSkuDao.findById(skuId);
    }


    /**
     * {@inheritDoc}
     */
    public List<Product> getProductByCategory(final long categoryId) {
        return productDao.findByNamedQuery("PRODUCTS.BY.CATEGORYID", categoryId, new Date());
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public Product getRandomProductByCategory(final Category category) {
        if (!category.getProductCategory().isEmpty()) {
            int idx = rand.nextInt(category.getProductCategory().size());
            ProductCategory productCategory = (ProductCategory) category.getProductCategory().toArray()[idx];
            return getById(productCategory.getProduct().getProductId());
        }
        return null;
    }



    /**
     * Get the grouped product attributes, with values.
     *
     * @param attributable product  or sku
     * @param productTypeId product type id
     * @return List of pair group names - list of attribute name and value.
     */
    public List<Pair<String, List<AttrValue>>> getProductAttributes(final Attributable attributable, final long productTypeId) {
        final ProductType productType = productTypeDao.findById(productTypeId);
        final Collection<ProdTypeAttributeViewGroup> attributeViewGroup = productType.getAttributeViewGroup();
        final List<Pair<String, List<AttrValue>>> attributesToShow =
                new ArrayList<Pair<String, List<AttrValue>>>(attributeViewGroup.size());
        Collection<AttrValue> attrValues = attributable.getAllAttibutes();

        for (ProdTypeAttributeViewGroup viewGroup : attributeViewGroup) {
            final List<AttrValue> attrNameValues = getProductAttributeValues(attrValues, viewGroup);
            attributesToShow.add(
                    new Pair<String, List<AttrValue>>(
                            viewGroup.getAttributeViewGroup().getName(),
                            attrNameValues)
            );
        }
        return attributesToShow;
    }


    private List<AttrValue> getProductAttributeValues(
            final Collection<AttrValue> attrValueCollection,
            final ProdTypeAttributeViewGroup viewGroup) {

        final String[] attributesNames = viewGroup.getAttrCodeList().split(",");
        final List<AttrValue> attrNameValues = new ArrayList<AttrValue>(attributesNames.length);
        for (String attrName : attributesNames) {
            for (AttrValue attrValue : attrValueCollection) {
                if (attrValue.getAttribute().getCode().equals(attrName)) {
                    attrNameValues.add(attrValue);
                }
            }
        }
        return attrNameValues;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public ProductSku getProductSkuByCode(final String skuCode) {
        final List<ProductSku> skus = productSkuDao.findByNamedQuery("PRODUCT.SKU.BY.CODE", skuCode);
        if (CollectionUtils.isEmpty(skus)) {
            return null;
            //throw new ObjectNotFoundException(ProductSku.class, "skuCode", skuCode);
        }
        return skus.get(0);
    }

    /**
     * Get product by sku code.
     *
     * @param skuCode sku code
     * @return product sku for this sku code
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public Product getProductBySkuCode(final String skuCode) {
        return (Product) productDao.getScalarResultByNamedQuery("PRODUCT.BY.SKU.CODE", skuCode);
    }


    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public Product getProductById(final Long productId) {
        return productDao.findById(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public List<Product> getProductByQuery(
            final Query query,
            final int firtsResult,
            final int maxResults) {

        return productDao.fullTextSearch(query, firtsResult, maxResults);

    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public List<Product> getFeaturedProducts(final List categories) {
        List<Product> list = productDao.findQueryObjectsByNamedQueryWithList("PRODUCT.FEATURED", categories, new Date());  //TODO v2 time machine
        Collections.shuffle(list);
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public List<Product> getProductByQuery(
            final Query query,
            final int firtsResult,
            final int maxResults,
            final String sortFieldName,
            final boolean reverse) {

        return productDao.fullTextSearch(query, firtsResult, maxResults, sortFieldName, reverse);

    }


    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public int getProductQty(final Query query) {
        return productDao.getResultCount(query);
    }


    /**
     * {@inheritDoc}
     */
    public List<Product> getProductByCategory(
            final long categoryId,
            final int firtsResult,
            final int maxResults) {
        return productDao.findByNamedQuery("PRODUCTS.BY.CATEGORYID",
                firtsResult,
                maxResults,
                categoryId,
                new Date()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public List<Object> getDistinctAttributeValues(final long productTypeId, final String code) {
        return productDao.findQueryObjectByNamedQuery(
                "PRODUCTS.ATTRIBUTE.VALUES.BY.CODE.PRODUCTTYPEID",
                productTypeId,
                code);
    }

    /**
     * {@inheritDoc}
     */
    public List<FiteredNavigationRecord> getDistinctBrands(final List categories) {
        List<Object[]> list = productDao.findQueryObjectsByNamedQueryWithList(
                "PRODUCTS.ATTR.CODE.VALUES.BY.ASSIGNED.CATEGORIES",
                categories);
        return constructBrandFilteredNavigationRecords(list);
    }


    /**
     * Get the ranked by ProductTypeAttr.rank list of unique product attribute values by given product type
     * and attribute code.
     *
     * @param productTypeId product type id
     * @return list of distinct attib values
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public List<FiteredNavigationRecord> getDistinctAttributeValues(final long productTypeId) {
        final List<FiteredNavigationRecord> records = new ArrayList<FiteredNavigationRecord>();
        records.addAll(getSingleValueNavigationRecords(productTypeId));
        records.addAll(getRangeValueNavigationRecords(productTypeId));
        Collections.sort(
                records,
                new Comparator<FiteredNavigationRecord>() {
                    public int compare(final FiteredNavigationRecord record1, final FiteredNavigationRecord record2) {
                        int rez = record1.getRank() - record2.getRank();
                        if (rez == 0) {
                            rez = record1.getName().compareTo(record2.getName());
                            if (rez == 0) {
                                rez = record1.getValue().compareTo(record2.getValue());
                            }
                        }
                        return rez;
                    }
                });
        return records;
    }

    /**
     * Get the navigation records for range values.
     *
     * @param productTypeId product type id
     * @return list of {@link FiteredNavigationRecord}
     */
    private List<FiteredNavigationRecord> getRangeValueNavigationRecords(final long productTypeId) {

        final ProductType productType = productTypeDao.findById(productTypeId);

        final List<FiteredNavigationRecord> records = new ArrayList<FiteredNavigationRecord>();

        for (ProductTypeAttr entry : productType.getAttribute()) {
            if (entry.getNavigationType().equals("R")) {
                RangeList<RangeNode> rangeList = entry.getRangeList();
                if (rangeList != null) {
                    for (RangeNode node : rangeList) {
                        records.add(
                                new FiteredNavigationRecordImpl(
                                        entry.getAttribute().getName(),
                                        entry.getAttribute().getCode(),
                                        node.getRange().getFirst() + '-' + node.getRange().getSecond(),
                                        node.getRange().getFirst() + '-' + node.getRange().getSecond(),
                                        0,
                                        entry.getRank(),
                                        "R"
                                )
                        );
                    }
                }
            }
        }
        return records;
    }

    /**
     * Construct filtered navigation records.
     *
     * @param list of raw object arrays after, result of named query
     * @return constructed list of navigation records.
     */
    private List<FiteredNavigationRecord> constructFilteredNavigationRecords(final List<Object[]> list) {
        List<FiteredNavigationRecord> result = new ArrayList<FiteredNavigationRecord>(list.size());
        for (Object[] objArray : list) {
            result.add(
                    new FiteredNavigationRecordImpl(
                            (String) objArray[0],
                            (String) objArray[1],
                            (String) objArray[2],
                            (String) objArray[3],
                            (Integer) objArray[4],
                            (Integer) objArray[5],
                            "S"
                    )
            );

        }
        return result;
    }


    /**
     * Collect the single attribute value navigation see ProductTypeAttr#navigationType
     *
     * @param productTypeId product type id
     * @return list of {@link FiteredNavigationRecord}
     */
    private List<FiteredNavigationRecord> getSingleValueNavigationRecords(final long productTypeId) {
        List<Object[]> list = productDao.findQueryObjectsByNamedQuery(
                "PRODUCTS.ATTR.CODE.VALUES.BY.PRODUCTTYPEID",
                productTypeId);
        return constructFilteredNavigationRecords(list);
    }


    /**
     * Construct filtered navigation records.
     *
     * @param list of raw object arrays after, result of named query
     * @return constructed list of navigation records.
     */
    private List<FiteredNavigationRecord> constructBrandFilteredNavigationRecords(final List<Object[]> list) {
        List<FiteredNavigationRecord> result = new ArrayList<FiteredNavigationRecord>(list.size());
        for (Object[] objArray : list) {
            result.add(
                    new FiteredNavigationRecordImpl(
                            (String) objArray[0],
                            (String) objArray[1],
                            (String) objArray[2],
                            (Integer) objArray[3]
                    )
            );

        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public Long getProductIdBySeoUri(final String seoUri) {
        List<Product> list = productDao.findByNamedQuery("PRODUCT.BY.SEO.URI", seoUri);
        if (list != null && !list.isEmpty()) {
            return list.get(0).getProductId();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public Long getProductSkuIdBySeoUri(final String seoUri) {
        List<ProductSku> list = productSkuDao.findByNamedQuery("SKU.BY.SEO.URI", seoUri);
        if (list != null && !list.isEmpty()) {
            return list.get(0).getSkuId();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Cacheable(value = PROD_SERV_METHOD_CACHE)
    public int getProductQty(final long categoryId) {
        return Integer.valueOf(
                String.valueOf(productDao.getScalarResultByNamedQuery("PRODUCTS.QTY.BY.CATEGORYID", categoryId, new Date())));
    }


    /**
     * {@inheritDoc}
     */
    public int reindexProducts() {
        return productDao.fullTextSearchReindex();
    }

    /**
     * {@inheritDoc}
     */
    public int reindexProduct(final Long pk) {
        return productDao.fullTextSearchReindex(pk);
    }

    /**
     * Get the total quantity of product skus on all warehouses.
     *
     * @param product product
     * @return quantity of product.
     */
    public BigDecimal getProductQuantity(final Product product) {
        return productDao.findSingleByNamedQuery("SKU.QTY.BY.PRODUCT", product);
    }

    /**
     * {@inheritDoc}
     */
    public BigDecimal getProductQuantity(final Product product, final Shop shop) {
        return productDao.findSingleByNamedQuery("SKU.QTY.BY.PRODUCT.SHOP", product, shop);
    }


    /**
     * {@inheritDoc}
     */
    public void clearEmptyAttributes() {
        productDao.executeNativeQuery("DELETE FROM TPRODUCTATTRVALUE WHERE VAL IS NULL OR VAL =''");
    }

    /**
     * {@inheritDoc}
     */

    public List<Product> getProductByConeNameBrandType(
            final String code,
            final String name,
            final Long brandId,
            final Long productTypeId) {

        final List<Criterion> criterionList = new ArrayList<Criterion>();
        if(code != null) {
            criterionList.add(Restrictions.like("code", code, MatchMode.ANYWHERE));
        }
        if(name != null) {
            criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
        }
        if(brandId != null) {
            criterionList.add(Restrictions.eq("brand.brandId", brandId));
        }
        if(productTypeId != null) {
            criterionList.add(Restrictions.eq("producttype.producttypeId", productTypeId));
        }

        return productDao.findByCriteria(
                criterionList.toArray(new Criterion[criterionList.size()])
        );

    }


}

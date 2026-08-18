import React, { useState } from 'react';
import { SellerLayout } from '../components/SellerLayout';
import { SellerProductTable } from '../components/SellerProductTable';
import { useCreateSellerProduct, useSellerProducts } from '../hooks/useSeller';
import { Button } from '../../../components/ui/Button';
import { Dialog } from '../../../components/ui/Dialog';
import { Input } from '../../../components/ui/Input';
import { Select } from '../../../components/ui/Select';

export const SellerProductManagementPage: React.FC = () => {
  const { data: products, isLoading } = useSellerProducts();
  const createProductMutation = useCreateSellerProduct();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [sku, setSku] = useState('');
  const [category, setCategory] = useState('Grain Spawn');
  const [price, setPrice] = useState('29.99');
  const [initialStock, setInitialStock] = useState('50');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !sku) return;

    createProductMutation.mutate(
      {
        name,
        sku,
        category,
        price: parseFloat(price) || 0,
        initialStock: parseInt(initialStock, 10) || 50,
      },
      {
        onSuccess: () => {
          setIsModalOpen(false);
          setName('');
          setSku('');
        },
      }
    );
  };

  return (
    <SellerLayout>
      <div data-testid="seller-product-management-page" className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-2xl font-bold text-slate-100 tracking-tight">Product Listings Manager</h2>
            <p className="text-sm text-slate-400">
              Create, inspect, and manage your marketplace catalog listings.
            </p>
          </div>
          <Button variant="primary" size="sm" onClick={() => setIsModalOpen(true)}>
            + Add New Product Listing
          </Button>
        </div>

        {/* Products Table */}
        <SellerProductTable products={products || []} isLoading={isLoading} />

        {/* Create Product Modal */}
        <Dialog isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Create Seller Product Listing">
          <form onSubmit={handleSubmit} className="space-y-4 py-2">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Product Title</label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Pink Oyster Mushroom Grain Spawn Bag 1kg"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">SKU Code</label>
                <Input
                  value={sku}
                  onChange={(e) => setSku(e.target.value)}
                  placeholder="e.g. SKU-PINK-OYSTER-1KG"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Category</label>
                <Select value={category} onChange={(e) => setCategory(e.target.value)}>
                  <option value="Grain Spawn">Grain Spawn</option>
                  <option value="Liquid Cultures">Liquid Cultures</option>
                  <option value="Plug Spawn">Plug Spawn</option>
                  <option value="Fruiting Kits">Fruiting Kits</option>
                </Select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Price (USD)</label>
                <Input
                  type="number"
                  step="0.01"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Initial Stock Units</label>
                <Input
                  type="number"
                  value={initialStock}
                  onChange={(e) => setInitialStock(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-4">
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={createProductMutation.isPending}>
                {createProductMutation.isPending ? 'Publishing...' : 'Publish Product Listing'}
              </Button>
            </div>
          </form>
        </Dialog>
      </div>
    </SellerLayout>
  );
};

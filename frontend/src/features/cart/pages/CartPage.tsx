import { FC, useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { useUpdateCartItem } from '../hooks/useUpdateCartItem';
import { useRemoveCartItem } from '../hooks/useRemoveCartItem';
import { useClearCart } from '../hooks/useClearCart';
import { PageShell } from '../../../components/layout/PageShell';
import { Breadcrumb } from '../../../components/ui/Breadcrumb';
import { Dialog } from '../../../components/ui/Dialog';
import { Button } from '../../../components/ui/Button';
import { CartItem } from '../components/CartItem';
import { CartSummary } from '../components/CartSummary';
import { CartEmptyState } from '../components/CartEmptyState';
import { CartSkeleton } from '../components/CartSkeleton';
import { CartErrorState } from '../components/CartErrorState';
import { CartValidationAlert } from '../components/CartValidationAlert';
import { isAuthenticated, getCartErrorMessage, CART_ERROR_CODES } from '../utils/cartUtils';
import { ApiError } from '../../../services/apiError';

/**
 * CartPage — authenticated customer cart experience.
 *
 * States: sign-in required → loading skeleton → fetch error (retry) →
 * empty cart → populated cart (items + order summary). Quantity and removal
 * mutations are server-confirmed: the UI only reflects backend responses, so
 * displayed totals can never diverge from the authoritative cart. Per-item
 * failures render inline and keep the item intact for recovery.
 */
export const CartPage: FC = () => {
  const { data: cartResponse, isLoading, isError, error, refetch } = useCart();
  const updateMutation = useUpdateCartItem();
  const removeMutation = useRemoveCartItem();
  const clearMutation = useClearCart();

  const [confirmClearOpen, setConfirmClearOpen] = useState(false);
  const [itemErrors, setItemErrors] = useState<Record<string, string>>({});
  const [unavailable, setUnavailable] = useState<Record<string, string>>({});
  const [cartLevelError, setCartLevelError] = useState<string | null>(null);

  const cart = cartResponse?.data;
  const items = cart?.items ?? [];
  const authenticated = isAuthenticated();

  const pendingUpdateItemId = updateMutation.isPending ? updateMutation.variables?.itemId : undefined;
  const pendingRemoveItemId = removeMutation.isPending ? removeMutation.variables : undefined;

  const clearItemError = useCallback((itemId: string) => {
    setItemErrors((prev) => {
      if (!(itemId in prev)) return prev;
      const next = { ...prev };
      delete next[itemId];
      return next;
    });
    setUnavailable((prev) => {
      if (!(itemId in prev)) return prev;
      const next = { ...prev };
      delete next[itemId];
      return next;
    });
  }, []);

  const handleQuantityChange = useCallback(
    (itemId: string, quantity: number) => {
      clearItemError(itemId);
      updateMutation.mutate(
        { itemId, quantity },
        {
          onSuccess: () => clearItemError(itemId),
          onError: (err: Error) => {
            if (err instanceof ApiError && err.code === CART_ERROR_CODES.PRODUCT_NOT_PURCHASABLE) {
              setUnavailable((prev) => ({ ...prev, [itemId]: getCartErrorMessage(err) }));
              return;
            }
            setItemErrors((prev) => ({ ...prev, [itemId]: getCartErrorMessage(err) }));
          },
        }
      );
    },
    [updateMutation, clearItemError]
  );

  const handleRemove = useCallback(
    (itemId: string) => {
      clearItemError(itemId);
      removeMutation.mutate(itemId, {
        onSuccess: () => clearItemError(itemId),
        onError: (err: Error) => {
          // Item already gone (e.g. stale cart) — reconcile with the backend
          // instead of showing a dead end.
          if (err instanceof ApiError && err.code === CART_ERROR_CODES.ITEM_NOT_FOUND) {
            void refetch();
            return;
          }
          setItemErrors((prev) => ({ ...prev, [itemId]: getCartErrorMessage(err) }));
        },
      });
    },
    [removeMutation, clearItemError, refetch]
  );

  const handleClearConfirm = useCallback(() => {
    setConfirmClearOpen(false);
    setCartLevelError(null);
    clearMutation.mutate(undefined, {
      onError: (err: Error) => setCartLevelError(getCartErrorMessage(err)),
    });
  }, [clearMutation]);

  const breadcrumbs = (
    <Breadcrumb items={[{ label: 'Home', path: '/' }, { label: 'Your Cart' }]} />
  );

  return (
    <PageShell title="Your Cart" breadcrumbs={breadcrumbs} className="cart-page">
      {!authenticated && (
        <CartValidationAlert
          variant="warning"
          title="Sign in required"
          message="Sign in to view and manage your cart. Your cart is tied to your authenticated account."
        />
      )}

      {!authenticated && (
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem' }}>
          <Link to="/products" className="btn btn-secondary btn-sm">
            Continue Shopping
          </Link>
        </div>
      )}

      {authenticated && isLoading && <CartSkeleton />}

      {authenticated && isError && (
        <CartErrorState error={error} onRetry={() => refetch()} />
      )}

      {authenticated && !isLoading && !isError && cart && items.length === 0 && <CartEmptyState />}

      {authenticated && !isLoading && !isError && cart && items.length > 0 && (
        <>
          {cartLevelError && <CartValidationAlert message={cartLevelError} />}

          <div className="cart-layout" data-testid="cart-layout">
            <section aria-label="Cart items" className="cart-items" style={{ minWidth: 0 }}>
              {items.map((item) => (
                <CartItem
                  key={item.id}
                  item={item}
                  currency={cart.currency || 'INR'}
                  isUpdating={pendingUpdateItemId === item.id}
                  isRemoving={pendingRemoveItemId === item.id}
                  error={itemErrors[item.id]}
                  unavailable={Boolean(unavailable[item.id])}
                  unavailableMessage={unavailable[item.id]}
                  onQuantityChange={(quantity) => handleQuantityChange(item.id, quantity)}
                  onRemove={() => handleRemove(item.id)}
                />
              ))}
            </section>

            <aside aria-label="Order summary">
              <CartSummary
                cart={cart}
                isClearing={clearMutation.isPending}
                onClearCart={() => setConfirmClearOpen(true)}
              />
            </aside>
          </div>

          <Dialog
            isOpen={confirmClearOpen}
            onClose={() => setConfirmClearOpen(false)}
            title="Clear your cart?"
            description="This will remove all items from your cart. This action cannot be undone."
          >
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <Button variant="secondary" onClick={() => setConfirmClearOpen(false)}>
                Cancel
              </Button>
              <Button variant="destructive" onClick={handleClearConfirm} isLoading={clearMutation.isPending}>
                Clear Cart
              </Button>
            </div>
          </Dialog>
        </>
      )}
    </PageShell>
  );
};
import { FC, useEffect } from 'react';

interface SEOHeadProps {
  title?: string;
  description?: string;
  keywords?: string;
  ogImage?: string;
}

export const SEOHead: FC<SEOHeadProps> = ({
  title = 'SPOREKART v3.0 — Mushroom Cultivation Marketplace & Academy',
  description = 'High-grade mushroom spawn, sterilized substrates, laboratory cultures, and professional mycology training.',
  keywords = 'mushroom spawn, oyster mushroom, spawn bag, substrate, mycology training, sporekart',
  ogImage = '/assets/sporekart-og.png',
}) => {
  useEffect(() => {
    document.title = title;

    const setMeta = (nameOrProperty: string, content: string, isProperty = false) => {
      const attribute = isProperty ? 'property' : 'name';
      let element = document.querySelector(`meta[${attribute}="${nameOrProperty}"]`);
      if (!element) {
        element = document.createElement('meta');
        element.setAttribute(attribute, nameOrProperty);
        document.head.appendChild(element);
      }
      element.setAttribute('content', content);
    };

    setMeta('description', description);
    setMeta('keywords', keywords);
    setMeta('og:title', title, true);
    setMeta('og:description', description, true);
    setMeta('og:image', ogImage, true);
    setMeta('twitter:card', 'summary_large_image');
  }, [title, description, keywords, ogImage]);

  return null;
};

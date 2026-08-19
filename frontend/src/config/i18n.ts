export type Language = 'en' | 'kn';

export const TRANSLATIONS: Record<Language, Record<string, string>> = {
  en: {
    appName: 'SPOREKART v3.0',
    tagline: 'Mushroom Cultivation Marketplace & Academy',
    home: 'Home',
    catalog: 'Catalog',
    categories: 'Categories',
    cart: 'Cart',
    checkout: 'Checkout',
    orders: 'Orders',
    growerPortal: 'Grower Portal',
    academy: 'Academy',
    adminConsole: 'Admin Console',
    searchPlaceholder: 'Search spawn, cultures, substrate...',
    addToCart: 'Add to Cart',
    outOfStock: 'Out of Stock',
    total: 'Total',
  },
  kn: {
    appName: 'ಸ್ಪೋರ್‌ಕಾರ್ಟ್ v3.0',
    tagline: 'ಅಣಬೆ ಬೆಳೆಗಾರರ ಮಾರುಕಟ್ಟೆ ಮತ್ತು ತರಬೇತಿ ಅಕಾಡೆಮಿ',
    home: 'ಮುಖ್ಯ ಪುಟ',
    catalog: 'ಉತ್ಪನ್ನಗಳು',
    categories: 'ವರ್ಗಗಳು',
    cart: 'ಕಾರ್ಟ್',
    checkout: 'ಖರೀದಿ ಪೂರ್ಣಗೊಳಿಸಿ',
    orders: 'ನನ್ನ ಆರ್ಡರ್‌ಗಳು',
    growerPortal: 'ಬೆಳೆಗಾರರ ಪೋರ್ಟಲ್',
    academy: 'ತರಬೇತಿ ಅಕಾಡೆಮಿ',
    adminConsole: 'ಆಡಳಿತ ಮಂಡಳಿ',
    searchPlaceholder: 'ಉತ್ಪನ್ನಗಳನ್ನು ಹುಡುಕಿ...',
    addToCart: 'ಕಾರ್ಟ್‌ಗೆ ಸೇರಿಸಿ',
    outOfStock: 'ಸ್ಟಾಕ್ ಲಭ್ಯವಿಲ್ಲ',
    total: 'ಒಟ್ಟು ಮೊತ್ತ',
  },
};

let currentLanguage: Language = 'en';

export const getLanguage = (): Language => currentLanguage;

export const setLanguage = (lang: Language): void => {
  currentLanguage = lang;
  localStorage.setItem('sporekart_lang', lang);
};

export const t = (key: string): string => {
  return TRANSLATIONS[currentLanguage]?.[key] || TRANSLATIONS.en[key] || key;
};

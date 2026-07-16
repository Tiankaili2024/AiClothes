<template>
  <div class="bg-layer">
    <svg class="bg-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1920 1080" preserveAspectRatio="xMidYMid slice">
      <defs>
        <filter id="pt"><feTurbulence type="fractalNoise" baseFrequency="0.7" numOctaves="5" seed="12" result="n"/>
          <feColorMatrix type="saturate" values="0" in="n" result="gn"/>
          <feComponentTransfer in="gn" result="sn"><feFuncA type="linear" slope="0.04"/></feComponentTransfer>
          <feBlend in="SourceGraphic" in2="sn" mode="multiply"/>
        </filter>
        <filter id="bl"><feGaussianBlur stdDeviation="20"/></filter>
        <linearGradient id="lw" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#E8F0F6"/><stop offset="0.4" stop-color="#E8EDF0"/>
          <stop offset="0.75" stop-color="#EDE4D6"/><stop offset="1" stop-color="#F0E6D8"/>
        </linearGradient>
        <linearGradient id="rc" x1="0" y1="1" x2="1" y2="0">
          <stop offset="0" stop-color="#C8D4DE"/><stop offset="0.5" stop-color="#D4DEE6"/><stop offset="1" stop-color="#E0E8EE"/>
        </linearGradient>
        <radialGradient id="cg" cx="50%" cy="50%" r="45%">
          <stop offset="0" stop-color="#F0E8DE" stop-opacity="0.12"/><stop offset="1" stop-color="#F0E8DE" stop-opacity="0"/>
        </radialGradient>
      </defs>
      <rect x="0" y="0" width="1056" height="1080" fill="url(#lw)" filter="url(#pt)"/>
      <rect x="864" y="0" width="1056" height="1080" fill="url(#rc)" filter="url(#pt)"/>
      <rect x="780" y="0" width="360" height="1080" fill="#D4CCBE" opacity="0.06" filter="url(#bl)"/>
      <rect x="800" y="0" width="320" height="1080" fill="#C4CCD6" opacity="0.05" filter="url(#bl)"/>
      <ellipse cx="280" cy="540" rx="260" ry="340" fill="#DCC8B4" opacity="0.035" filter="url(#bl)"/>
      <ellipse cx="1620" cy="540" rx="240" ry="360" fill="#B0C0CE" opacity="0.035" filter="url(#bl)"/>
      <rect width="1920" height="1080" fill="url(#cg)"/>
      <g fill="none" stroke="#9CADBA" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" opacity="0.26" transform="translate(90,160)">
        <path d="M50,10 L45,70 Q42,110 48,160 L52,210 Q55,250 48,300 L44,330 Q40,355 35,370"/>
        <path d="M120,10 L125,70 Q128,110 122,160 L118,210 Q115,250 122,300 L126,330 Q130,355 135,370"/>
        <path d="M50,10 L85,24 L120,10"/><path d="M58,15 L70,38 L85,30"/><path d="M112,15 L100,38 L85,30"/>
        <g stroke-width="0.65">
          <path d="M42,50 L128,50"/><path d="M40,90 L130,90"/><path d="M42,130 L128,130"/><path d="M44,170 L126,170"/>
          <path d="M46,210 L124,210"/><path d="M45,250 L125,250"/><path d="M44,290 L126,290"/><path d="M38,340 L132,340"/>
          <path d="M60,10 L56,370"/><path d="M75,18 L70,370"/><path d="M95,18 L100,370"/><path d="M110,10 L114,370"/>
        </g>
        <circle cx="85" cy="80" r="2.3" fill="#AAB8C6" stroke="none"/>
        <circle cx="85" cy="115" r="2.3" fill="#AAB8C6" stroke="none"/><circle cx="85" cy="150" r="2.3" fill="#AAB8C6" stroke="none"/>
        <path d="M55,200 L78,200"/><path d="M112,200 L92,200"/>
      </g>
      <g fill="none" stroke="#A8B8C5" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" opacity="0.28" transform="translate(1500,140)">
        <path d="M45,-8 Q45,-26 22.5,-26 Q0,-26 0,-8"/><path d="M0,-5 L0,4"/><path d="M0,0 L90,0"/><path d="M90,0 L90,-5"/>
        <g stroke="#9CADBA" stroke-width="1.4">
          <path d="M10,8 L7,55 Q5,85 9,125 L13,170 Q15,205 10,250 L7,285 Q4,310 0,330"/>
          <path d="M80,8 L83,55 Q85,85 81,125 L77,170 Q75,205 80,250 L83,285 Q86,310 90,330"/>
          <path d="M10,8 L45,20 L80,8"/><path d="M20,12 L32,34 L45,27"/><path d="M70,12 L58,34 L45,27"/>
          <path d="M7,135 Q45,126 83,135"/><path d="M7,146 Q45,137 83,146"/>
        </g>
        <circle cx="45" cy="65" r="2.3" fill="#9CAEBC" stroke="none"/>
        <circle cx="45" cy="95" r="2.3" fill="#9CAEBC" stroke="none"/><circle cx="45" cy="170" r="2.3" fill="#9CAEBC" stroke="none"/>
        <g stroke="#9CADBA" stroke-width="1.4"><path d="M16,190 L38,190"/><path d="M74,190 L52,190"/><path d="M0,330 Q22.5,320 45,330 Q67.5,320 90,330"/></g>
        <g stroke="#A8B8C6" stroke-width="0.75"><path d="M22,45 Q27,90 25,135"/><path d="M68,45 Q63,90 65,135"/></g>
      </g>
    </svg>
  </div>
</template>
<style scoped>
.bg-layer { position: absolute; inset: 0; z-index: 0; }
.bg-svg { display: block; width: 100%; height: 100%; }
</style>

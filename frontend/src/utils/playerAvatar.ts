export const AVATAR_NAMES = [
  'male1',
  'male2',
  'male3',
  'male4',
  'male5',
  'male6',
  'male7',
  'male8',
  'male9',
  'female1',
  'female2',
  'female3',
  'female4',
  'female5',
  'female6',
  'female7',
  'female8',
  'female9',
  'unknown',
] as const;

export const getRandomAvatar = () => AVATAR_NAMES[Math.floor(Math.random() * (AVATAR_NAMES.length - 1))];

export const getAvatarUrl = (avatar: string) => require(`../assets/images/avatars/${avatar}.png`);

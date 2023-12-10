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

export const AVATARS: Record<string, string> = AVATAR_NAMES.reduce(
  (result, av) => ({
    ...result,
    [av]: require(`../assets/images/avatars/${av}.png`),
  }),
  {} as Record<string, string>
);

export const getRandomAvatar = () => {
  const urls = Object.values(AVATARS);
  return urls[Math.floor(Math.random() * (urls.length - 1))];
}

export const getAvatar = (avatar: string) => require(`../assets/images/avatars/${avatar}.png`);

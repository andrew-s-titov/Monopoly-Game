import StartPageBackground from "../components/StartPageBackground";
import { useAuthContext } from "../context/AuthContextProvider";
import StartPageCenteredContent from "../components/StartPageCenteredContent";

const NotFoundPage = () => {

  const { isLoggedIn } = useAuthContext();

  return (
    <StartPageBackground withHeader={!!isLoggedIn}>
      <StartPageCenteredContent>
        <div className="not-found-title">404</div>
        <div className="not-found-description">Page not found</div>
      </StartPageCenteredContent>
    </StartPageBackground>
  );
}

export default NotFoundPage;

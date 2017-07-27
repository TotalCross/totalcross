package tc.tools.deployer.ipa;

public interface ElephantMemoryStream
{
  public abstract int getPos();

  public abstract void moveTo(int newPosition);

  public abstract void memorize();

  public abstract void moveBack();
}

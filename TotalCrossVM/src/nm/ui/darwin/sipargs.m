#define Object NSObject*
#include "sipargs.h"
#include "mainview.h"

SipArgs
SipArgsMake(int options, id control, bool numeric, NSString* text)
{
  SipArgs args;
  args.options = options;
  args.control = control;
  args.numeric = numeric;
  return args;
}

@implementation SipArguments

- (id)init:(SipArgs)args
{
  v = args;
  return self;
}

- (SipArgs)values
{
  return v;
}

- (void)dealloc
{
  [super dealloc];
}

@end

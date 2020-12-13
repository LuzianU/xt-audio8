#ifndef XT_DSOUND_PRIVATE_HPP
#define XT_DSOUND_PRIVATE_HPP
#if XT_ENABLE_DSOUND

#include <xt/api/public/Enums.h>
#include <xt/private/Shared.hpp>
#include <string>

inline double const
XtiDsMinBufferMs = 100.0;
inline double const
XtiDsMaxBufferMs = 5000.0;
inline double const
XtiDsDefaultBufferMs = 500.0;

inline double const
XtiDsMinSampleRate = 8000.0;
inline double const
XtiDsMaxSampleRate = 192000.0;

struct XtDsDeviceInfo
{
  std::string id;
  std::string name;
  bool output;
};

char const* 
XtiGetDSoundFaultText(XtFault fault);
XtCause 
XtiGetDSoundFaultCause(XtFault fault);

#endif // XT_ENABLE_DSOUND
#endif // XT_DSOUND_PRIVATE_HPP
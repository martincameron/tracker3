
public class ModPlay3
{
	private static final int NUM_SAMPLES = 31;
	private static final int MAX_CHANNELS = 16;
	private static final int FIXED_POINT_SHIFT = 13;
	private static final int FIXED_POINT_ONE = 1 << FIXED_POINT_SHIFT;
	
	private int sampleRate;
	
	private String songName;
	private String[] instrumentNames = new String[ NUM_SAMPLES ];
	private int numChannels;
	private int songLength;
	private int numPatterns;
	private int c2Rate;
	private byte[] sequence;
	private byte[] patternData;
	private byte[][] sampleData = new byte[ NUM_SAMPLES ][];
	private byte[] sampleFineTune = new byte[ NUM_SAMPLES ];
	private byte[] sampleVolume = new byte[ NUM_SAMPLES ];
	private int[] sampleLoopStart = new int[ NUM_SAMPLES ];
	private int[] sampleLoopLength = new int[ NUM_SAMPLES ];
	private int[] channelInstrument = new int[ MAX_CHANNELS ];
	private int[] channelVolume = new int[ MAX_CHANNELS ];
	private int[] channelPanning = new int[ MAX_CHANNELS ];
	private int[] channelPeriod = new int[ MAX_CHANNELS ];
	private int[] channelSamplePos = new int[ MAX_CHANNELS ];
	private int[] channelStep = new int[ MAX_CHANNELS ];
	
	private int tickSamplePos;
	private int tickSampleLen;
	
	private int currentSequencePos;
	private int nextSequencePos;
	private int currentRow;
	private int nextRow;
	private int currentTick;
	private int ticksPerRow = 6;
	private int tempo = 125;
	
	public ModPlay3( int sampleRate, java.io.InputStream moduleData ) throws java.io.IOException
	{
		this.sampleRate = sampleRate;
		songName = readString( moduleData, 20 ).trim();
System.out.println(songName);
		int[] sampleLengths = new int[ NUM_SAMPLES ];
		for( int idx = 0; idx < NUM_SAMPLES; idx++ )
		{
			instrumentNames[ idx ] = readString( moduleData, 22 ).trim();
			sampleLengths[ idx ] = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			sampleFineTune[ idx ] = ( byte ) ( moduleData.read() & 0xF );
			sampleVolume[ idx ] = ( byte ) ( moduleData.read() & 0x7F );
			int loopStart = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			int loopLength = ( ( ( moduleData.read() & 0xFF ) << 8 ) | ( moduleData.read() & 0xFF ) ) * 2;
			if( loopLength < 2 || loopStart > sampleLengths[ idx ] )
			{
				loopStart = sampleLengths[ idx ];
			}
			if( loopStart + loopLength > sampleLengths[ idx ] )
			{
				loopLength = sampleLengths[ idx ] - loopStart;
			}
			sampleLoopStart[ idx ]  = loopStart * FIXED_POINT_ONE;
			sampleLoopLength[ idx ]  = loopLength * FIXED_POINT_ONE;
if(instrumentNames[idx].length() > 0 )System.out.println(instrumentNames[idx]);
		}
		songLength = moduleData.read() & 0x7F;
		int restart = moduleData.read() & 0x7F;
		sequence = readBytes( moduleData, 128 );
		for( int idx = 0; idx < 128; idx++ )
		{
			if( numPatterns < sequence[ idx ] + 1 ) 
			{
				numPatterns = sequence[ idx ] + 1;
			}
		}
		String modType = readString( moduleData, 4 );
		if( modType.equals( "M.K." ) )
		{
			numChannels = 4;
			c2Rate = 8287;
		}
		else
		{
			throw new IllegalArgumentException( "Module not recognised!" );
		}
		patternData = readBytes( moduleData, numChannels * 4 * 64 * numPatterns );
		for( int idx = 0; idx < NUM_SAMPLES; idx++ )
		{
			sampleData[ idx ] = readBytes( moduleData, sampleLengths[ idx ] );
		}
	}
	
	public void getAudio( int[] output, int count )
	{
		if( tickSamplePos++ >= tickSampleLen )
		{
			tickSamplePos = 0;
			tickSampleLen = ( sampleRate * 5 ) / ( tempo * 2 );
			tick();
		}
		for( int idx = 0, end = count * 2; idx < end; idx += 2 )
		{
			int lamp = 0, ramp = 0;
			for( int chn = 0; chn < numChannels; chn++ )
			{
				int instrument = channelInstrument[ chn ];
				int loopLength = sampleLoopLength[ instrument ];
				int loopEnd = sampleLoopStart[ instrument ] + loopLength;
				int samplePos = channelSamplePos[ chn ];
				if( samplePos < loopEnd )
				{
					int amplitude = sampleData[ instrument ][ samplePos >> FIXED_POINT_SHIFT ] * channelVolume[ chn ];
					lamp += ( amplitude * ( FIXED_POINT_ONE - channelPanning[ chn ] ) ) >> FIXED_POINT_SHIFT;
					ramp += ( amplitude * channelPanning[ chn ] ) >> FIXED_POINT_SHIFT;
					samplePos += channelStep[ chn ];
					if( loopLength > 0 )
					{
						while( samplePos >= loopEnd )
						{
							samplePos -= loopLength;
						}
					}
					channelSamplePos[ chn ] = samplePos;
				}
			}
			output[ idx ] = lamp;
			output[ idx + 1 ] = ramp;
		}
	}
	
	private void row()
	{
		currentSequencePos = nextSequencePos;
		currentRow = nextRow;
		currentTick = ticksPerRow;
		nextRow = currentRow + 1;
		if( nextRow > 63 )
		{
			nextRow = 0;
			nextSequencePos = currentSequencePos + 1;
			if( nextSequencePos >= songLength )
			{
				nextSequencePos = 0;
			}
		}
	}
	
	private void tick()
	{
		if( currentTick-- <= 0 )
		{
			row();
		}
		for( int chn = 0; chn < numChannels; chn++ ) 
		{
			channelStep[ chn ] = ( c2Rate * 428 / channelPeriod[ chn ] ) * FIXED_POINT_ONE / sampleRate;
		}
	}
	
	private static byte[] readBytes( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		byte[] bytes = new byte[ length ];
		int offset = 0;
		int count = 0;
		while( offset < length && count >= 0 )
		{
			offset += count;
			count = inputStream.read( bytes, offset, length - offset );
		}
		return bytes;
	}
	
	private static String readString( java.io.InputStream inputStream, int length ) throws java.io.IOException
	{
		return new String( readBytes( inputStream, length ), "ISO-8859-1" );
	}
	
	public static void main( String[] args ) throws java.io.IOException {
		java.io.InputStream inputStream = new java.io.FileInputStream( args[ 0 ] );
		try
		{
			ModPlay3 modPlay3 = new ModPlay3( 48000, inputStream );
		}
		finally
		{
			inputStream.close();
		}
	}
}
